package com.example.kalyan_kosh_api.portal;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PortalDatabaseProvisioner {

    private static final Logger log = LoggerFactory.getLogger(PortalDatabaseProvisioner.class);
    private static final String FLYWAY_HISTORY_TABLE = "flyway_schema_history";

    private final PortalDatabaseProperties properties;

    public PortalDatabaseProvisioner(PortalDatabaseProperties properties) {
        this.properties = properties;
    }

    public void prepareDatabases() {
        validateIdentifier(properties.getSourceDatabase(), "source database");

        if (!databaseExists(properties.getSourceDatabase())) {
            throw new IllegalStateException(
                    "Source database '" + properties.getSourceDatabase()
                            + "' does not exist. Start the current single-portal backend/database first."
            );
        }

        for (PortalCode portalCode : properties.getEnabledPortalCodes()) {
            String databaseName = properties.getDatabase(portalCode).getDatabaseName();
            validateIdentifier(databaseName, portalCode.name() + " database");

            if (!databaseExists(databaseName)) {
                if (!properties.isAutoCreateDatabases()) {
                    throw new IllegalStateException(
                            "Database '" + databaseName + "' is missing and automatic creation is disabled."
                    );
                }
                createDatabase(databaseName);
            }

            if (!databaseName.equalsIgnoreCase(properties.getSourceDatabase())) {
                cloneMissingSchemaTables(properties.getSourceDatabase(), databaseName);
            }

            migrateDatabase(portalCode, databaseName);
        }
    }

    private boolean databaseExists(String databaseName) {
        String sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";
        try (Connection connection = openServerConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, databaseName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to check database '" + databaseName + "'.", ex);
        }
    }

    private void createDatabase(String databaseName) {
        String sql = "CREATE DATABASE IF NOT EXISTS `" + databaseName
                + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
        try (Connection connection = openServerConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
            log.info("Created portal database: {}", databaseName);
        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Unable to create database '" + databaseName
                            + "'. Ensure the configured MySQL user has CREATE DATABASE permission.",
                    ex
            );
        }
    }

    private void cloneMissingSchemaTables(String sourceDatabase, String targetDatabase) {
        List<String> sourceTableNames = readTableNames(sourceDatabase);
        Set<String> targetTableNames = new HashSet<>(readTableNames(targetDatabase));
        List<String> tableNames = sourceTableNames.stream()
                .filter(tableName -> !targetTableNames.contains(tableName))
                .toList();
        if (sourceTableNames.isEmpty()) {
            throw new IllegalStateException(
                    "Source database '" + sourceDatabase + "' does not contain application tables."
            );
        }

        if (tableNames.isEmpty()) {
            return;
        }

        log.info("Cloning {} missing table definitions from {} to {}", tableNames.size(), sourceDatabase, targetDatabase);

        try (Connection sourceConnection = openDatabaseConnection(sourceDatabase);
             Connection targetConnection = openDatabaseConnection(targetDatabase);
             Statement targetStatement = targetConnection.createStatement()) {

            targetStatement.execute("SET FOREIGN_KEY_CHECKS = 0");
            try {
                for (String tableName : tableNames) {
                    String createSql = readCreateTableSql(sourceConnection, sourceDatabase, tableName);
                    targetStatement.execute(createSql);
                }
            } finally {
                targetStatement.execute("SET FOREIGN_KEY_CHECKS = 1");
            }

            log.info("Schema clone completed for database: {}", targetDatabase);
        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Unable to clone schema from '" + sourceDatabase + "' to '" + targetDatabase + "'.",
                    ex
            );
        }
    }

    private List<String> readTableNames(String sourceDatabase) {
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE' AND TABLE_NAME <> ? ORDER BY TABLE_NAME";
        List<String> names = new ArrayList<>();

        try (Connection connection = openServerConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sourceDatabase);
            statement.setString(2, FLYWAY_HISTORY_TABLE);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    names.add(resultSet.getString(1));
                }
            }
            return names;
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to read database tables for '" + sourceDatabase + "'.", ex);
        }
    }

    private String readCreateTableSql(Connection connection, String databaseName, String tableName) throws SQLException {
        validateIdentifier(tableName, "table");
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SHOW CREATE TABLE `" + databaseName + "`.`" + tableName + "`"
             )) {
            if (!resultSet.next()) {
                throw new SQLException("SHOW CREATE TABLE returned no result for " + tableName);
            }
            return resultSet.getString(2);
        }
    }

    private void migrateDatabase(PortalCode portalCode, String databaseName) {
        List<String> migrationLocations = new ArrayList<>();
        migrationLocations.add("classpath:db/migration");

        if (portalCode == PortalCode.TAB2) {
            migrationLocations.add("classpath:db/portal/tab2");
        }

        Flyway flyway = Flyway.configure()
                .dataSource(properties.buildJdbcUrl(databaseName), properties.getUsername(), properties.getPassword())
                .locations(migrationLocations.toArray(new String[0]))
                .baselineOnMigrate(true)
                .baselineVersion(MigrationVersion.fromVersion(properties.getFlywayBaselineVersion()))
                .baselineDescription("Multi-portal schema baseline")
                .load();

        flyway.migrate();
        log.info("Flyway migration check completed for database: {}", databaseName);
    }

    private Connection openServerConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.buildServerJdbcUrl(),
                properties.getUsername(),
                properties.getPassword()
        );
    }

    private Connection openDatabaseConnection(String databaseName) throws SQLException {
        return DriverManager.getConnection(
                properties.buildJdbcUrl(databaseName),
                properties.getUsername(),
                properties.getPassword()
        );
    }

    private void validateIdentifier(String value, String label) {
        if (value == null || !value.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Invalid " + label + " name: " + value);
        }
    }
}

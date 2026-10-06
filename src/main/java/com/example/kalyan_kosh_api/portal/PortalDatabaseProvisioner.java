package com.example.kalyan_kosh_api.portal;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
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
    private static final String COMMON_FLYWAY_HISTORY_TABLE = "flyway_schema_history";

    private final PortalDatabaseProperties properties;
    private final PortalAvailabilityRegistry availabilityRegistry;

    public PortalDatabaseProvisioner(
            PortalDatabaseProperties properties,
            PortalAvailabilityRegistry availabilityRegistry
    ) {
        this.properties = properties;
        this.availabilityRegistry = availabilityRegistry;
    }

    /**
     * Prepares every configured portal independently.
     *
     * The default portal is required and remains fail-fast. Optional portals are
     * isolated: if one cannot be prepared, it is marked FAILED and application
     * startup continues so the primary portal remains available.
     */
    public void prepareDatabases() {
        validateIdentifier(properties.getSourceDatabase(), "source database");

        PortalCode defaultPortal = properties.getDefaultPortalCode();
        if (!properties.isPortalEnabled(defaultPortal)) {
            throw new IllegalStateException("Default portal is disabled: " + defaultPortal.name());
        }

        String defaultDatabase = properties.getDatabase(defaultPortal).getDatabaseName();
        if (defaultDatabase == null
                || !defaultDatabase.equalsIgnoreCase(properties.getSourceDatabase())) {
            throw new IllegalStateException(
                    "The default portal database must be the configured source database. "
                            + "Default=" + defaultDatabase + ", source=" + properties.getSourceDatabase()
            );
        }

        if (!databaseExists(properties.getSourceDatabase())) {
            throw new IllegalStateException(
                    "Source database '" + properties.getSourceDatabase()
                            + "' does not exist. The primary portal database must exist before startup."
            );
        }

        for (PortalCode portalCode : PortalCode.values()) {
            if (!properties.isPortalEnabled(portalCode)) {
                availabilityRegistry.markDisabled(portalCode);
            }
        }

        // Always prepare the primary/default portal first. Its failure is fatal.
        prepareRequiredPortal(defaultPortal);

        // Optional portals are isolated from the primary portal.
        for (PortalCode portalCode : properties.getEnabledPortalCodes()) {
            if (portalCode == defaultPortal) {
                continue;
            }
            prepareOptionalPortal(portalCode);
        }
    }

    private void prepareRequiredPortal(PortalCode portalCode) {
        availabilityRegistry.markPreparing(portalCode);
        try {
            preparePortal(portalCode);
            availabilityRegistry.markReady(portalCode);
            log.info("Portal {} database is READY", portalCode.name());
        } catch (RuntimeException ex) {
            availabilityRegistry.markFailed(portalCode, ex);
            log.error("Required portal {} failed database preparation", portalCode.name(), ex);
            throw ex;
        }
    }

    private void prepareOptionalPortal(PortalCode portalCode) {
        availabilityRegistry.markPreparing(portalCode);
        try {
            preparePortal(portalCode);
            availabilityRegistry.markReady(portalCode);
            log.info("Optional portal {} database is READY", portalCode.name());
        } catch (RuntimeException ex) {
            availabilityRegistry.markFailed(portalCode, ex);
            log.error(
                    "Optional portal {} failed database preparation and will remain unavailable. "
                            + "The primary portal will continue running. Reason: {}",
                    portalCode.name(),
                    ex.getMessage(),
                    ex
            );
        }
    }

    private void preparePortal(PortalCode portalCode) {
        String databaseName = properties.getDatabase(portalCode).getDatabaseName();
        validateIdentifier(databaseName, portalCode.name() + " database");

        boolean isSourceDatabase = databaseName.equalsIgnoreCase(properties.getSourceDatabase());
        if (portalCode != properties.getDefaultPortalCode() && isSourceDatabase) {
            throw new IllegalStateException(
                    "Optional portal " + portalCode.name()
                            + " cannot use the primary/source database '" + databaseName + "'."
            );
        }

        boolean databaseCreated = false;

        if (!databaseExists(databaseName)) {
            if (!properties.isAutoCreateDatabases()) {
                throw new IllegalStateException(
                        "Database '" + databaseName + "' is missing and automatic creation is disabled. "
                                + "Provision it during deployment before enabling " + portalCode.name() + "."
                );
            }
            createDatabase(databaseName);
            databaseCreated = true;
        }

        if (!isSourceDatabase) {
            initializeOptionalPortalSchemaIfNeeded(databaseName, databaseCreated);
        }

        migrateCommonDatabase(databaseName);
        migratePortalSpecificDatabase(portalCode, databaseName);
    }

    /**
     * A new optional portal starts from a snapshot of the current primary schema.
     * We then baseline COMMON Flyway history at the source database's current
     * version so historical common migrations are not replayed against columns
     * and tables already present in the cloned schema.
     */
    private void initializeOptionalPortalSchemaIfNeeded(String databaseName, boolean databaseCreated) {
        boolean hasApplicationTables = hasApplicationTables(databaseName);
        boolean hasCommonHistory = tableExists(databaseName, COMMON_FLYWAY_HISTORY_TABLE);

        if (!hasApplicationTables) {
            cloneMissingSchemaTables(properties.getSourceDatabase(), databaseName);
            baselineCommonHistoryAtSourceVersion(databaseName);
            return;
        }

        if (!hasCommonHistory) {
            throw new IllegalStateException(
                    "Database '" + databaseName + "' already contains application tables but has no "
                            + COMMON_FLYWAY_HISTORY_TABLE + ". Refusing to guess migration history. "
                            + "Back up and explicitly baseline/recreate this optional portal before enabling it."
            );
        }

        if (databaseCreated) {
            // Defensive only: a just-created DB should have been empty above.
            throw new IllegalStateException(
                    "New portal database '" + databaseName + "' unexpectedly contained application tables."
            );
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

    private boolean hasApplicationTables(String databaseName) {
        return !readTableNames(databaseName).isEmpty();
    }

    private boolean tableExists(String databaseName, String tableName) {
        String sql = "SELECT 1 FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND TABLE_TYPE = 'BASE TABLE' LIMIT 1";
        try (Connection connection = openServerConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, databaseName);
            statement.setString(2, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Unable to inspect table '" + tableName + "' in database '" + databaseName + "'.",
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

        log.info(
                "Cloning {} table definitions from {} to new/empty portal database {}",
                tableNames.size(),
                sourceDatabase,
                targetDatabase
        );

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

    private List<String> readTableNames(String databaseName) {
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE' "
                + "AND TABLE_NAME NOT LIKE 'flyway\\_%\\_schema\\_history' ESCAPE '\\\\' "
                + "AND TABLE_NAME <> ? ORDER BY TABLE_NAME";
        List<String> names = new ArrayList<>();

        try (Connection connection = openServerConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, databaseName);
            statement.setString(2, COMMON_FLYWAY_HISTORY_TABLE);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    names.add(resultSet.getString(1));
                }
            }
            return names;
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to read database tables for '" + databaseName + "'.", ex);
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

    private void baselineCommonHistoryAtSourceVersion(String targetDatabase) {
        String sourceVersion = readCurrentSuccessfulFlywayVersion(properties.getSourceDatabase());
        if (sourceVersion == null || sourceVersion.isBlank()) {
            sourceVersion = properties.getFlywayBaselineVersion();
        }

        Flyway flyway = Flyway.configure()
                .dataSource(properties.buildJdbcUrl(targetDatabase), properties.getUsername(), properties.getPassword())
                .locations("classpath:db/migration")
                .table(COMMON_FLYWAY_HISTORY_TABLE)
                .baselineVersion(MigrationVersion.fromVersion(sourceVersion))
                .baselineDescription("Cloned from " + properties.getSourceDatabase() + " at common version " + sourceVersion)
                .load();

        flyway.baseline();
        log.info("Baselined common migration history for {} at version {}", targetDatabase, sourceVersion);
    }

    private String readCurrentSuccessfulFlywayVersion(String databaseName) {
        if (!tableExists(databaseName, COMMON_FLYWAY_HISTORY_TABLE)) {
            return properties.getFlywayBaselineVersion();
        }

        String sql = "SELECT version FROM `" + databaseName + "`.`" + COMMON_FLYWAY_HISTORY_TABLE + "` "
                + "WHERE success = 1 AND version IS NOT NULL ORDER BY installed_rank DESC LIMIT 1";
        try (Connection connection = openServerConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next() ? resultSet.getString(1) : properties.getFlywayBaselineVersion();
        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Unable to determine current Flyway version for source database '" + databaseName + "'.",
                    ex
            );
        }
    }

    private void migrateCommonDatabase(String databaseName) {
        Flyway flyway = Flyway.configure()
                .dataSource(properties.buildJdbcUrl(databaseName), properties.getUsername(), properties.getPassword())
                .locations("classpath:db/migration")
                .table(COMMON_FLYWAY_HISTORY_TABLE)
                .baselineOnMigrate(true)
                .baselineVersion(MigrationVersion.fromVersion(properties.getFlywayBaselineVersion()))
                .baselineDescription("Multi-portal common schema baseline")
                .load();

        flyway.migrate();
        log.info("Common Flyway migration check completed for database: {}", databaseName);
    }

    /**
     * Portal-specific migrations use their own history table. This prevents a
     * portal migration version (for example TAB2 V9) from colliding with common
     * migration versions in flyway_schema_history.
     */
    private void migratePortalSpecificDatabase(PortalCode portalCode, String databaseName) {
        if (portalCode == PortalCode.TAB1) {
            return;
        }

        String location = "classpath:db/portal/" + portalCode.getSlug();
        String historyTable = "flyway_" + portalCode.getSlug() + "_schema_history";

        Flyway flyway = Flyway.configure()
                .dataSource(properties.buildJdbcUrl(databaseName), properties.getUsername(), properties.getPassword())
                .locations(location)
                .table(historyTable)
                .baselineOnMigrate(true)
                .baselineVersion(MigrationVersion.fromVersion("0"))
                .baselineDescription(portalCode.name() + " portal-specific baseline")
                .load();

        flyway.migrate();
        log.info("{} specific Flyway migration check completed for database: {}", portalCode.name(), databaseName);
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

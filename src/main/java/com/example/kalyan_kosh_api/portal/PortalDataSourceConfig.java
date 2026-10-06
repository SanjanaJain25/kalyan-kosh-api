package com.example.kalyan_kosh_api.portal;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(PortalDatabaseProperties.class)
public class PortalDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(PortalDataSourceConfig.class);

    @Bean
    @Primary
    public DataSource dataSource(
            PortalDatabaseProperties properties,
            PortalDatabaseProvisioner provisioner,
            PortalAvailabilityRegistry availabilityRegistry
    ) {
        provisioner.prepareDatabases();

        Map<Object, Object> targetDataSources = new LinkedHashMap<>();

        for (PortalCode portalCode : availabilityRegistry.getReadyPortalCodes()) {
            String databaseName = properties.getDatabase(portalCode).getDatabaseName();
            targetDataSources.put(portalCode, createDataSource(properties, portalCode, databaseName));
            log.info("Registered datasource for READY portal {} -> {}", portalCode.name(), databaseName);
        }

        PortalCode defaultPortal = properties.getDefaultPortalCode();
        Object defaultDataSource = targetDataSources.get(defaultPortal);
        if (defaultDataSource == null) {
            throw new IllegalStateException(
                    "Default portal datasource is not READY: " + defaultPortal
                            + " (status=" + availabilityRegistry.getStatus(defaultPortal) + ")"
            );
        }

        PortalRoutingDataSource routingDataSource = new PortalRoutingDataSource();
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(defaultDataSource);
        routingDataSource.setLenientFallback(false);
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }

    private DataSource createDataSource(
            PortalDatabaseProperties properties,
            PortalCode portalCode,
            String databaseName
    ) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(properties.buildJdbcUrl(databaseName));
        hikariConfig.setUsername(properties.getUsername());
        hikariConfig.setPassword(properties.getPassword());
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setPoolName("KalyanKosh-" + portalCode.name());
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setValidationTimeout(5000);
        return new HikariDataSource(hikariConfig);
    }
}

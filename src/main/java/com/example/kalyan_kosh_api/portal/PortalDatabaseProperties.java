package com.example.kalyan_kosh_api.portal;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "app.portal")
public class PortalDatabaseProperties {

    private String defaultCode = "TAB1";
    private boolean requireHeader = false;
    private boolean autoCreateDatabases = false;
    private String serverUrl = "jdbc:mysql://localhost:3306";
    private String username = "root";
    private String password = "";
    private String jdbcParameters = "allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
    private String sourceDatabase = "kalyankosh_db";
    private String flywayBaselineVersion = "8";
    private Map<String, PortalDatabase> databases = new LinkedHashMap<>();

    public String getDefaultCode() {
        return defaultCode;
    }

    public void setDefaultCode(String defaultCode) {
        this.defaultCode = defaultCode;
    }

    public boolean isRequireHeader() {
        return requireHeader;
    }

    public void setRequireHeader(boolean requireHeader) {
        this.requireHeader = requireHeader;
    }

    public boolean isAutoCreateDatabases() {
        return autoCreateDatabases;
    }

    public void setAutoCreateDatabases(boolean autoCreateDatabases) {
        this.autoCreateDatabases = autoCreateDatabases;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getJdbcParameters() {
        return jdbcParameters;
    }

    public void setJdbcParameters(String jdbcParameters) {
        this.jdbcParameters = jdbcParameters;
    }

    public String getSourceDatabase() {
        return sourceDatabase;
    }

    public void setSourceDatabase(String sourceDatabase) {
        this.sourceDatabase = sourceDatabase;
    }

    public String getFlywayBaselineVersion() {
        return flywayBaselineVersion;
    }

    public void setFlywayBaselineVersion(String flywayBaselineVersion) {
        this.flywayBaselineVersion = flywayBaselineVersion;
    }

    public Map<String, PortalDatabase> getDatabases() {
        return databases;
    }

    public void setDatabases(Map<String, PortalDatabase> databases) {
        this.databases = databases;
    }

    public PortalCode getDefaultPortalCode() {
        PortalCode portalCode = PortalCode.fromValue(defaultCode);
        return portalCode != null ? portalCode : PortalCode.TAB1;
    }

    public PortalDatabase getDatabase(PortalCode portalCode) {
        if (portalCode == null) {
            throw new IllegalArgumentException("Portal code is required.");
        }

        PortalDatabase database = databases.get(portalCode.getSlug());
        if (database == null) {
            database = databases.get(portalCode.name().toLowerCase());
        }
        if (database == null) {
            throw new IllegalStateException("Database configuration missing for " + portalCode.name());
        }
        return database;
    }

    public boolean isPortalEnabled(PortalCode portalCode) {
        return getDatabase(portalCode).isEnabled();
    }

    public boolean isDefaultPortal(PortalCode portalCode) {
        return getDefaultPortalCode() == portalCode;
    }

    public List<PortalCode> getEnabledPortalCodes() {
        List<PortalCode> enabled = new ArrayList<>();
        for (PortalCode portalCode : PortalCode.values()) {
            PortalDatabase config = getDatabase(portalCode);
            if (config.isEnabled()) {
                enabled.add(portalCode);
            }
        }
        return enabled;
    }

    public String buildJdbcUrl(String databaseName) {
        String base = serverUrl.endsWith("/")
                ? serverUrl.substring(0, serverUrl.length() - 1)
                : serverUrl;
        String params = jdbcParameters == null || jdbcParameters.isBlank()
                ? ""
                : "?" + jdbcParameters;
        return base + "/" + databaseName + params;
    }

    public String buildServerJdbcUrl() {
        String params = jdbcParameters == null || jdbcParameters.isBlank()
                ? ""
                : "?" + jdbcParameters;
        return serverUrl + (serverUrl.contains("?") ? "" : params);
    }

    public static class PortalDatabase {
        private boolean enabled = false;
        private String databaseName;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }
    }
}

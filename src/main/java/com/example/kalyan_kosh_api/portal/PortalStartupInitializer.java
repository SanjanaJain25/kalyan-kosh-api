package com.example.kalyan_kosh_api.portal;

import com.example.kalyan_kosh_api.config.LocationSeeder;
import com.example.kalyan_kosh_api.config.SuperAdminSeeder;
import com.example.kalyan_kosh_api.config.SettingsInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class PortalStartupInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PortalStartupInitializer.class);

    private final PortalDatabaseProperties properties;
    private final TransactionTemplate transactionTemplate;
    private final LocationSeeder locationSeeder;
    private final SuperAdminSeeder superAdminSeeder;
    private final SettingsInitializer settingsInitializer;

    public PortalStartupInitializer(
            PortalDatabaseProperties properties,
            TransactionTemplate transactionTemplate,
            LocationSeeder locationSeeder,
            SuperAdminSeeder superAdminSeeder,
            SettingsInitializer settingsInitializer
    ) {
        this.properties = properties;
        this.transactionTemplate = transactionTemplate;
        this.locationSeeder = locationSeeder;
        this.superAdminSeeder = superAdminSeeder;
        this.settingsInitializer = settingsInitializer;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (PortalCode portalCode : properties.getEnabledPortalCodes()) {
            PortalContext.runWith(portalCode, () ->
                    transactionTemplate.executeWithoutResult(status -> {
                        log.info("Initializing seed data for {}", portalCode.name());
                        locationSeeder.seedIfMissing();
                        settingsInitializer.initializeDefaults();
                        superAdminSeeder.seedIfMissing();
                    })
            );
        }
    }
}

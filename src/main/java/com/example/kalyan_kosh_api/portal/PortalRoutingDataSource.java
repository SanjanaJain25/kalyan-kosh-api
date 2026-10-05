package com.example.kalyan_kosh_api.portal;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class PortalRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return PortalContext.get();
    }
}

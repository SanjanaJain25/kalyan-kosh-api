package com.example.kalyan_kosh_api.portal;

import java.util.Locale;

/**
 * Supported application portals.
 */
public enum PortalCode {
    TAB1("tab1"),
    TAB2("tab2"),
    TAB3("tab3");

    private final String slug;

    PortalCode(String slug) {
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }

    public static PortalCode fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (PortalCode portalCode : values()) {
            if (portalCode.name().equals(normalized)
                    || portalCode.slug.equalsIgnoreCase(value.trim())) {
                return portalCode;
            }
        }

        throw new IllegalArgumentException("Unsupported portal code: " + value);
    }
}

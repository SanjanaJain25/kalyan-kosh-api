package com.example.kalyan_kosh_api.util;

/** Nullable TAB1 employment service type. Existing users need not populate it. */
public final class ServiceTypeValues {
    private ServiceTypeValues() {}

    public static String normalize(String input) {
        if (input == null || input.isBlank()) return null;
        String value = input.trim();
        if (!"नियमित".equals(value) && !"संविदा".equals(value)) {
            throw new IllegalArgumentException("Invalid serviceType; allowed values are नियमित and संविदा");
        }
        return value;
    }
}

package com.example.kalyan_kosh_api.portal;

/**
 * Stores the portal selected for the current request/thread.
 */
public final class PortalContext {

    private static final ThreadLocal<PortalCode> CURRENT = new ThreadLocal<>();

    private PortalContext() {
    }

    public static void set(PortalCode portalCode) {
        CURRENT.set(portalCode);
    }

    public static PortalCode get() {
        PortalCode portalCode = CURRENT.get();
        return portalCode != null ? portalCode : PortalCode.TAB1;
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static void runWith(PortalCode portalCode, Runnable action) {
        PortalCode previous = CURRENT.get();
        try {
            set(portalCode);
            action.run();
        } finally {
            if (previous == null) {
                clear();
            } else {
                set(previous);
            }
        }
    }
}

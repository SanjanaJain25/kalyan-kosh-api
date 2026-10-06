package com.example.kalyan_kosh_api.portal;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime availability state for each configured portal.
 *
 * TAB1 is the primary portal. Optional portals may fail preparation without
 * taking the whole application down; their state is exposed here so routing
 * and startup initialization can safely skip them.
 */
@Component
public class PortalAvailabilityRegistry {

    public enum Status {
        DISABLED,
        PREPARING,
        READY,
        FAILED
    }

    private final Map<PortalCode, Status> statuses = Collections.synchronizedMap(new EnumMap<>(PortalCode.class));
    private final Map<PortalCode, String> failureReasons = Collections.synchronizedMap(new EnumMap<>(PortalCode.class));

    public PortalAvailabilityRegistry() {
        for (PortalCode portalCode : PortalCode.values()) {
            statuses.put(portalCode, Status.DISABLED);
        }
    }

    public void markDisabled(PortalCode portalCode) {
        statuses.put(portalCode, Status.DISABLED);
        failureReasons.remove(portalCode);
    }

    public void markPreparing(PortalCode portalCode) {
        statuses.put(portalCode, Status.PREPARING);
        failureReasons.remove(portalCode);
    }

    public void markReady(PortalCode portalCode) {
        statuses.put(portalCode, Status.READY);
        failureReasons.remove(portalCode);
    }

    public void markFailed(PortalCode portalCode, Throwable error) {
        statuses.put(portalCode, Status.FAILED);
        String message = error == null ? null : error.getMessage();
        failureReasons.put(
                portalCode,
                message == null || message.isBlank() ? "Portal preparation failed." : message
        );
    }

    public Status getStatus(PortalCode portalCode) {
        return statuses.getOrDefault(portalCode, Status.DISABLED);
    }

    public boolean isReady(PortalCode portalCode) {
        return getStatus(portalCode) == Status.READY;
    }

    public String getFailureReason(PortalCode portalCode) {
        return failureReasons.get(portalCode);
    }

    public List<PortalCode> getReadyPortalCodes() {
        List<PortalCode> ready = new ArrayList<>();
        for (PortalCode portalCode : PortalCode.values()) {
            if (isReady(portalCode)) {
                ready.add(portalCode);
            }
        }
        return ready;
    }
}

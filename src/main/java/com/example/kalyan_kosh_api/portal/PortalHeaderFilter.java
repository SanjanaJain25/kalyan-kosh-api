package com.example.kalyan_kosh_api.portal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class PortalHeaderFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Portal-Code";

    private final PortalDatabaseProperties properties;
    private final PortalAvailabilityRegistry availabilityRegistry;

    public PortalHeaderFilter(
            PortalDatabaseProperties properties,
            PortalAvailabilityRegistry availabilityRegistry
    ) {
        this.properties = properties;
        this.availabilityRegistry = availabilityRegistry;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // CORS preflight must not require a portal header. No business data is
        // accessed during OPTIONS requests.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String headerValue = request.getHeader(HEADER_NAME);

        try {
            PortalCode portalCode;

            if (headerValue == null || headerValue.isBlank()) {
                if (properties.isRequireHeader()) {
                    writePortalResponse(
                            response,
                            HttpServletResponse.SC_BAD_REQUEST,
                            "INVALID_PORTAL",
                            "Missing required " + HEADER_NAME + " header."
                    );
                    return;
                }
                portalCode = properties.getDefaultPortalCode();
            } else {
                portalCode = PortalCode.fromValue(headerValue);
            }

            if (!properties.isPortalEnabled(portalCode)) {
                writePortalResponse(
                        response,
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "PORTAL_DISABLED",
                        "Portal is not enabled: " + portalCode.name()
                );
                return;
            }

            if (!availabilityRegistry.isReady(portalCode)) {
                PortalAvailabilityRegistry.Status status = availabilityRegistry.getStatus(portalCode);
                writePortalResponse(
                        response,
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "PORTAL_UNAVAILABLE",
                        "Portal is currently unavailable: " + portalCode.name() + " (" + status.name() + ")"
                );
                return;
            }

            PortalContext.set(portalCode);
            response.setHeader(HEADER_NAME, portalCode.name());
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            writePortalResponse(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_PORTAL",
                    ex.getMessage()
            );
        } finally {
            PortalContext.clear();
        }
    }

    private void writePortalResponse(
            HttpServletResponse response,
            int status,
            String errorCode,
            String message
    ) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        String safeMessage = message == null ? "Invalid portal." : message.replace("\"", "'");
        response.getWriter().write(
                "{\"success\":false,\"errorCode\":\"" + errorCode + "\",\"message\":\""
                        + safeMessage + "\"}"
        );
    }
}

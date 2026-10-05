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

    public PortalHeaderFilter(PortalDatabaseProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String headerValue = request.getHeader(HEADER_NAME);

        try {
            PortalCode portalCode;

            if (headerValue == null || headerValue.isBlank()) {
                if (properties.isRequireHeader()) {
                    writeInvalidPortalResponse(response, "Missing required " + HEADER_NAME + " header.");
                    return;
                }
                portalCode = properties.getDefaultPortalCode();
            } else {
                portalCode = PortalCode.fromValue(headerValue);
            }

            if (!properties.getDatabase(portalCode).isEnabled()) {
                writeInvalidPortalResponse(response, "Portal is disabled: " + portalCode.name());
                return;
            }

            PortalContext.set(portalCode);
            response.setHeader(HEADER_NAME, portalCode.name());
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            writeInvalidPortalResponse(response, ex.getMessage());
        } finally {
            PortalContext.clear();
        }
    }

    private void writeInvalidPortalResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        String safeMessage = message == null ? "Invalid portal." : message.replace("\"", "'");
        response.getWriter().write(
                "{\"success\":false,\"errorCode\":\"INVALID_PORTAL\",\"message\":\""
                        + safeMessage + "\"}"
        );
    }
}

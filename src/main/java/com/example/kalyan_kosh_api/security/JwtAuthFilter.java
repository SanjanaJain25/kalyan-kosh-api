package com.example.kalyan_kosh_api.security;

import com.example.kalyan_kosh_api.portal.PortalCode;
import com.example.kalyan_kosh_api.portal.PortalContext;
import com.example.kalyan_kosh_api.service.SystemSettingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService uds;
    private final SystemSettingService systemSettingService;

    public JwtAuthFilter(
            JwtUtil jwtUtil,
            CustomUserDetailsService uds,
            SystemSettingService systemSettingService
    ) {
        this.jwtUtil = jwtUtil;
        this.uds = uds;
        this.systemSettingService = systemSettingService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain
    ) throws ServletException, IOException {

        String header = req.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                jwtUtil.validate(token);

                PortalCode requestPortal = PortalContext.get();
                PortalCode tokenPortal = jwtUtil.extractPortal(token);

                // Old tokens without a portal claim are accepted only for TAB1.
                boolean portalMismatch = tokenPortal == null
                        ? requestPortal != PortalCode.TAB1
                        : tokenPortal != requestPortal;

                if (portalMismatch) {
                    writeUnauthorized(res, "Token does not belong to the selected portal. Please login again.");
                    return;
                }

                Instant globalLogoutAfter = systemSettingService.getGlobalForceLogoutAfter();
                Date issuedAtDate = jwtUtil.extractIssuedAt(token);

                if (globalLogoutAfter != null && issuedAtDate != null) {
                    Instant tokenIssuedAt = issuedAtDate.toInstant();

                    if (tokenIssuedAt.isBefore(globalLogoutAfter)) {
                        writeUnauthorized(res, "Session expired. Please login again.");
                        return;
                    }
                }

                String userId = jwtUtil.extractUsername(token);

                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userDetails = uds.loadUserByUsername(userId);

                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ex) {
                // Keep the request unauthenticated. Protected endpoints will return 401/403.
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(req, res);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message.replace("\"", "'") + "\"}");
    }
}

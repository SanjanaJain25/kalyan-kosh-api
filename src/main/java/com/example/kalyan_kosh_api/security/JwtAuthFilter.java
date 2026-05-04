package com.example.kalyan_kosh_api.security;

import com.example.kalyan_kosh_api.service.SystemSettingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
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
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                jwtUtil.validate(token);

                Instant globalLogoutAfter = systemSettingService.getGlobalForceLogoutAfter();
                Date issuedAtDate = jwtUtil.extractIssuedAt(token);

                if (globalLogoutAfter != null && issuedAtDate != null) {
                    Instant tokenIssuedAt = issuedAtDate.toInstant();

                    if (tokenIssuedAt.isBefore(globalLogoutAfter)) {
                        SecurityContextHolder.clearContext();
                        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        res.setContentType("application/json");
                        res.getWriter().write("{\"message\":\"Session expired. Please login again.\"}");
                        return;
                    }
                }

                String userId = jwtUtil.extractUsername(token);

                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var ud = uds.loadUserByUsername(userId);

                    var auth = new UsernamePasswordAuthenticationToken(
                            ud,
                            null,
                            ud.getAuthorities()
                    );

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(req, res);
    }
}
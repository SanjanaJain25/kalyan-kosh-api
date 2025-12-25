package com.example.kalyan_kosh_api.config;

import com.example.kalyan_kosh_api.security.CustomUserDetailsService;
import com.example.kalyan_kosh_api.security.JwtAuthFilter;
import com.example.kalyan_kosh_api.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService uds;
    private final JwtUtil jwtUtil;

    public SecurityConfig(CustomUserDetailsService uds, JwtUtil jwtUtil) {
        this.uds = uds;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtUtil, uds);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        return http
                // Disable CSRF (JWT based)
                .csrf(csrf -> csrf.disable())

                // Stateless session
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public APIs
                        .requestMatchers("/api/auth/**", "/api/locations/**").permitAll()

                        // Admin APIs
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Manager APIs
                        .requestMatchers("/api/manager/**").hasAnyRole("MANAGER", "ADMIN")

                        // ✅ USER can upload receipts
                        .requestMatchers("/api/receipts/**").hasRole("USER")

                        // Any other request needs authentication
                        .anyRequest().authenticated()
                )

                // JWT filter
                .addFilterBefore(jwtAuthFilter(),
                        UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}

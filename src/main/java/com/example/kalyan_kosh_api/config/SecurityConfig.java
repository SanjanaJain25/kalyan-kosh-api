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
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                // Enable CORS (uses corsConfigurationSource bean)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Disable CSRF (JWT based)
                .csrf(csrf -> csrf.disable())

                // Stateless session
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public APIs
                        // Allow unauthenticated GET access to location endpoints used by frontend
                        .requestMatchers(HttpMethod.GET, "/api/locations/**").permitAll()
                        // Allow preflight OPTIONS for any endpoint (so browser preflight won't be blocked)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Keep auth endpoints public
                        .requestMatchers("/api/auth/**").permitAll()
                        // Make only GET /api/users (getAllUsers) public, not individual user access
                        // Allow both /api/users and /api/users/ (with trailing slash)
                        .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/").permitAll()
                        // Allow the Spring error page/endpoint (so errors can be returned to anonymous callers)
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/email-otp/send").permitAll()
                        .requestMatchers("/api/auth/otp/verify").permitAll()

                        .requestMatchers(
                                "/api/auth/**",
                                "/api/auth/otp/**",
                                "/api/auth/forgot-password/**",
                                "/api/locations/**",
                                "/error"
                        ).permitAll()
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow the dev frontend (change to specific origin(s) in production)
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:3000", "*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

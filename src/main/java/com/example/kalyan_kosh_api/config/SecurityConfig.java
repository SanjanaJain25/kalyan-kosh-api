package com.example.kalyan_kosh_api.config;

import com.example.kalyan_kosh_api.security.CustomUserDetailsService;
import com.example.kalyan_kosh_api.security.JwtAuthFilter;
import com.example.kalyan_kosh_api.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
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

                // Set authentication provider
                .authenticationProvider(authenticationProvider())

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow preflight OPTIONS for any endpoint (CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public endpoints - Authentication & Registration
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public endpoints - Email OTP
                        .requestMatchers("/api/auth/email-otp/**").permitAll()

                        // Public endpoints - Locations
                        .requestMatchers("/api/locations/**").permitAll()

                        // Public endpoint - Get all users
                        .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/").permitAll()

                        // Public endpoint - Get users with pagination
                        .requestMatchers(HttpMethod.GET, "/api/users/paginated").permitAll()

                        // Public endpoint - Get users with filters
                        .requestMatchers(HttpMethod.GET, "/api/users/filter").permitAll()

                        // Public endpoint - User registration
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()

                        // Public endpoint - Error handling
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/email-otp/send").permitAll()
                        .requestMatchers("/api/auth/otp/verify").permitAll()

                        // Public endpoint - Admin Utils (for development/fixing data)
                        .requestMatchers("/api/admin/utils/**").permitAll()

                        // Public admin endpoints (must be before /api/admin/** rule)
                        .requestMatchers(HttpMethod.GET, "/api/admin/non-donors/paginated").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/monthly-sahyog/non-donors").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/monthly-sahyog/donors").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/dashboard/summary").permitAll()

                        // Admin APIs - requires ADMIN role (general rule - must come after specific permitAll rules)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/login").permitAll()

                        // Manager APIs - requires MANAGER or ADMIN role
                        .requestMatchers("/api/manager/**").hasAnyRole("MANAGER", "ADMIN")

                        // User APIs - requires USER role
                        .requestMatchers("/api/receipts/**").hasRole("USER")
                        .requestMatchers("/api/death-cases/**").hasRole("USER")

                        // Any other request needs authentication
                        .anyRequest().authenticated()
                )

                // JWT filter
                .addFilterBefore(jwtAuthFilter(),
                        UsernamePasswordAuthenticationFilter.class)

                .build();
    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // Allow development and production origins
//        configuration.setAllowedOriginPatterns(List.of(
//                "http://localhost:*",           // Local development
//                "http://127.0.0.1:*",           // Local IP
//                "https://pmums.com",            // Production domain (frontend)
//                "https://www.pmums.com",        // Production www
//                "https://backend.pmums.com",    // Production backend
//                "http://pmums.com",             // HTTP version
//                "http://www.pmums.com"          // HTTP www
//        ));
//
//        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
//        configuration.setAllowedHeaders(List.of("*"));
//        configuration.setExposedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
//        configuration.setAllowCredentials(true);   // ✅ Allow credentials (cookies, auth headers)
//        configuration.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // ✅ Exact origins only (IMPORTANT)
    configuration.setAllowedOrigins(List.of(
            "https://pmums.com",
            "https://www.pmums.com",
            "http://localhost:3000",
            "http://127.0.0.1:3000"
    ));

    configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
    ));

    configuration.setAllowedHeaders(List.of("*"));
    configuration.setExposedHeaders(List.of("Authorization"));
    configuration.setAllowCredentials(true); // works now
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}

}

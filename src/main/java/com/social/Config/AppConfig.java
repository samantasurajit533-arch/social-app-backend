package com.social.Config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class AppConfig {

    @Autowired
    private JwtValidator jwtValidator;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.sessionManagement(management -> management.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS));

        // Apply CORS before CSRF and Authorization
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // 1. Allow Preflight OPTIONS for all paths
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Public Auth and WebSocket endpoints
                        .requestMatchers("/auth/**", "/api/auth/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        // 3. AI Endpoints (Permit All for easy React access)
                        .requestMatchers("/api/ai/**").permitAll()

                        // 4. All other API calls require valid JWT
                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().permitAll())
                .addFilterBefore(jwtValidator, BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // Keeps WebSockets separate from standard security filters
        return (web) -> web.ignoring().requestMatchers("/ws/**");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // Specific allowed origins for Vercel and Localhost
        cfg.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "https://vercel.app"
        ));

        // Allowed patterns for Vercel preview deployments
        cfg.setAllowedOriginPatterns(Collections.singletonList("https://*.vercel.app"));

        cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        cfg.setAllowCredentials(true);

        // Critical headers for modern browser preflight checks
        cfg.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        cfg.setExposedHeaders(Collections.singletonList("Authorization"));
        cfg.setMaxAge(3600L); // Cache CORS preflight for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

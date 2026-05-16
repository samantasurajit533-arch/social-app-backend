package com.social.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class AppConfig {

    @Autowired
    private JwtValidator jwtValidator;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 1. CORS FIRST
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Disable CSRF
                .csrf(csrf -> csrf.disable())

                // 3. Stateless session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. AUTH RULES
                .authorizeHttpRequests(auth -> auth

                        // IMPORTANT: allow preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // PUBLIC APIs
                        .requestMatchers(
                                "/auth/**",
                                "/api/auth/**",
                                "/ws/**",
                                "/api/ai/**"
                        ).permitAll()

                        // PROTECTED APIs
                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().permitAll()
                )

                // 5. JWT FILTER
                .addFilterBefore(jwtValidator, BasicAuthenticationFilter.class);

        return http.build();
    }

    // ================= CORS CONFIG =================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration cfg = new CorsConfiguration();

        cfg.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "https://social-app-frontend-silk.vercel.app",
                "https://*.vercel.app"
        ));

        cfg.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        cfg.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin"
        ));

        cfg.setAllowCredentials(true);

        cfg.setExposedHeaders(Arrays.asList("Authorization"));

        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", cfg);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
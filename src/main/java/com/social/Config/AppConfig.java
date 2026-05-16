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

import java.util.List;

@Configuration
@EnableWebSecurity
public class AppConfig {

    @Autowired
    private JwtValidator jwtValidator;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 1. CORS MUST BE FIRST
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Disable CSRF for JWT APIs
                .csrf(csrf -> csrf.disable())

                // 3. Stateless session (IMPORTANT for JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // IMPORTANT: allow preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // public APIs
                        .requestMatchers(
                                "/auth/**",
                                "/api/auth/**",
                                "/ws/**",
                                "/api/ai/**"
                        ).permitAll()

                        // protected APIs
                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().permitAll()
                )

                // 5. JWT filter AFTER cors + auth rules
                .addFilterBefore(jwtValidator, BasicAuthenticationFilter.class);

        return http.build();
    }

    // =======================
    // CORS CONFIG (IMPORTANT)
    // =======================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "https://social-app-frontend-silk.vercel.app",
                "https://*.vercel.app",
                "http://localhost:3000"
        ));

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS",
                "PATCH"
        ));

        config.setAllowedHeaders(List.of("*"));

        config.setAllowCredentials(true);

        config.setExposedHeaders(List.of("Authorization"));

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    // =======================
    // PASSWORD ENCODER
    // =======================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
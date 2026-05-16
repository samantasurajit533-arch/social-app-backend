package com.social.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtValidator extends OncePerRequestFilter {

    @Autowired
    private JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ==============================
        // 1. ALWAYS ALLOW PRE-FLIGHT REQUESTS
        // ==============================
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // ==============================
        // 2. PUBLIC ENDPOINTS (NO JWT REQUIRED)
        // ==============================
        if (path.startsWith("/auth") ||
                path.startsWith("/api/auth") ||
                path.startsWith("/ws") ||
                path.startsWith("/api/ai")) {

            filterChain.doFilter(request, response);
            return;
        }

        // ==============================
        // 3. GET TOKEN FROM HEADER
        // ==============================
        String jwt = request.getHeader(JwtConstant.JWT_HEADER);

        if (jwt != null && jwt.startsWith("Bearer ")) {

            try {

                String token = jwt.substring(7);

                String email = jwtProvider.getEmailFromJwtToken(token);

                if (email != null) {

                    Authentication authentication =
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    null,
                                    Collections.emptyList()
                            );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }

            } catch (Exception e) {

                SecurityContextHolder.clearContext();

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");

                response.getWriter().write(
                        "{\"error\":\"Invalid or expired token\"}"
                );

                return;
            }
        }

        // ==============================
        // 4. CONTINUE FILTER CHAIN
        // ==============================
        filterChain.doFilter(request, response);
    }
}
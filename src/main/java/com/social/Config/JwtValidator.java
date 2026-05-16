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

        // ✅ 1. Fast-track OPTIONS requests to let AppConfig handle CORS handshakes
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // ✅ 2. Bypass rules for totally public paths including your new Vertex AI route
        if (path.startsWith("/ws") ||
                path.startsWith("/auth") ||
                path.startsWith("/api/auth") ||
                path.startsWith("/api/ai")) {

            filterChain.doFilter(request, response);
            return;
        }

        // ✅ 3. Extract and parse standard Bearer Token payloads
        String jwt = request.getHeader(JwtConstant.JWT_HEADER);

        if (jwt != null && jwt.startsWith("Bearer ")) {
            try {
                String token = jwt.substring(7).trim();
                String email = jwtProvider.getEmailFromJwtToken(token);

                if (email != null) {
                    Authentication authentication =
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    null,
                                    null // Inject authorities collection here later if roles are introduced
                            );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }

            } catch (Exception e) {
                SecurityContextHolder.clearContext();

                // Clear out preflight conflicts by enforcing matching header states inside failure pipelines
                response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
                response.setHeader("Access-Control-Allow-Credentials", "true");

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid or expired token context\"}");
                return;
            }
        } else {
            // FIX: If hitting a protected route without any token headers, reject early with a clean status
            if (path.startsWith("/api/")) {
                response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Missing authorization bearer credentials\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

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

        // ✅ 1. Allow OPTIONS (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // ✅ 2. Public endpoints
        if (path.startsWith("/ws") ||
                path.startsWith("/auth") ||
                path.startsWith("/api/auth") ||
                path.startsWith("/api/ai")) {

            filterChain.doFilter(request, response);
            return;
        }

        // ✅ 3. Get JWT token
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
                                    null
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

        filterChain.doFilter(request, response);
    }
}
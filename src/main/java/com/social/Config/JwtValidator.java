package com.social.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class JwtValidator extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = request.getHeader(JwtConstant.JWT_HEADER);

        // 1. Skip validation for WebSockets
        if (request.getRequestURI().startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. If no token, just proceed (Spring Security will handle the 403 based on your AppConfig)
        if (jwt != null) {
            try {
                // Remove "Bearer " prefix
                jwt = jwt.substring(7);

                String email = JwtProvider.getEmailFromJwtToken(jwt);

                Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                throw new BadCredentialsException("Invalid token...");
            }
        }

        filterChain.doFilter(request, response);
    }
}

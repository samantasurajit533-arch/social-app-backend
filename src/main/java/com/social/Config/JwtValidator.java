package com.social.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // CRITICAL MOBILE & CORS FIX: Return immediately with HTTP 200 OK for any OPTIONS pre-flight check
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String jwt = request.getHeader(JwtConstant.JWT_HEADER);

        String path = request.getRequestURI();
        if (path.startsWith("/ws") || path.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (jwt != null && jwt.startsWith("Bearer ")) {
            try {
                // Ensure your provider strips "Bearer " string if not handled inside the method
                String token = jwt.substring(7);
                String email = jwtProvider.getEmailFromJwtToken(token);

                Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // Prevent server crash loops—clear security context logs gracefully
                SecurityContextHolder.clearContext();
                throw new BadCredentialsException("Invalid token...");
            }
        }

        filterChain.doFilter(request, response);
    }
}

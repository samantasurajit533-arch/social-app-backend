package com.social.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtValidator extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    // ✅ Explicit Constructor Injection
    public JwtValidator(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

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
                                    java.util.Collections.emptyList()
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                // ✅ FIX: Do NOT call bare return. Let the chain finish so CORS headers attach!
                filterChain.doFilter(request, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

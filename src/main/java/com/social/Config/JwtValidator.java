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

        // ১. CORS প্রিফ্লাইট হ্যান্ডশেকের জন্য OPTIONS রিকোয়েস্ট সরাসরি পাস করা
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // ২. রিকোয়েস্ট হেডার থেকে টোকেন বের করা
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
                                    null // ফিউচারে রোল অ্যাড করলে এখানে বসবে
                            );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }

            } catch (Exception e) {
                SecurityContextHolder.clearContext();

                response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid or expired token context\"}");
                return;
            }
        }

        // ৩. সমস্ত রিকোয়েস্ট ফিল্টার চেইনে পাস করে দেওয়া (এটি স্প্রিং সিকিউরিটিকে চেইন মেইনটেইন করতে সাহায্য করবে)
        filterChain.doFilter(request, response);
    }
}

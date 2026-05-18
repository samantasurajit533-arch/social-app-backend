package com.social.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(Authentication auth) {

        SecretKey key = Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.builder()
                .setIssuer("Surajit")
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 86400000)
                )
                .claim("email", auth.getName())
                .signWith(key)
                .compact();
    }

    public String getEmailFromJwtToken(String jwt) {

        try {

            if (jwt == null) {
                return null;
            }

            if (jwt.startsWith("Bearer ")) {
                jwt = jwt.substring(7);
            }

            SecretKey key = Keys.hmacShaKeyFor(
                    secretKey.getBytes(StandardCharsets.UTF_8)
            );

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();

            return claims.get("email", String.class);

        } catch (Exception e) {

            System.out.println("JWT Error: " + e.getMessage());

            return null;
        }
    }
}
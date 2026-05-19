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

    // Helper method to guarantee a cryptographically secure key of proper length
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        // If the configured key is too short, pad it to 32 bytes (256 bits) to prevent crashes
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            return Keys.hmacShaKeyFor(paddedKey);
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication auth) {
        SecretKey key = getSigningKey();

        return Jwts.builder()
                .setIssuer("Surajit")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
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
                jwt = jwt.substring(7).trim();
            }

            SecretKey key = getSigningKey();

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();

            return claims.get("email", String.class);

        } catch (Exception e) {
            System.out.println("JWT Parsing Error: " + e.getMessage());
            return null;
        }
    }
}

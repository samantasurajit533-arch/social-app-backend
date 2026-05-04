package com.social.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import javax.crypto.SecretKey;
import java.util.Date;

public class JwtProvider {

    private static SecretKey key = Keys.hmacShaKeyFor(JwtConstant.SERECT_KEY.getBytes());

    public static String generateToken(Authentication auth) {
        return Jwts.builder()
                .setIssuer("Surajit")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .claim("email", auth.getName())
                .signWith(key)
                .compact();
    }

    public static String getEmailFromJwtToken(String jwt) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();

            return String.valueOf(claims.get("email"));
        } catch (Exception e) {
            return null;
        }
    }
}

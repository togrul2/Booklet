package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.entities.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;


enum TokenType {
    ACCESS,
    REFRESH
}

@Service
public class JwtService {
    @Value("${spring.security.jwt.secret-key}")
    private String secretKey;
    @Value("${spring.security.jwt.access-expiration:300000}")
    private long accessTokenExpiration;  // In milliseconds.
    @Value("${spring.security.jwt.refresh-expiration:86400000}")
    private long refreshTokenExpiration;  // In milliseconds.

    @NonNull
    private SecretKey getSecretKey() {
        byte[] decodedKey = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(String token, @NonNull Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Role extractRole(String jwtToken) {
        return extractClaim(jwtToken, claims -> Role.valueOf((String) claims.get("role")));
    }

    private TokenType extractTokenType(String token) {
        return extractClaim(token, claims -> TokenType.valueOf((String) claims.get("type")));
    }

    private String createToken(
            Map<String, Object> claims,
            @NonNull UserDetails userDetails,
            long expiration
    ) {
        final Date expirationDate = new Date(System.currentTimeMillis() + expiration);
        return Jwts
                .builder()
                .header()
                .type("JWT")
                .and()
                .subject(userDetails.getUsername())
                .claims(claims)
                .expiration(expirationDate)
                .signWith(getSecretKey())
                .compact();
    }

    public boolean isAccessToken(String token) {
        return extractTokenType(token).equals(TokenType.ACCESS);
    }

    public boolean isRefreshToken(String token) {
        return extractTokenType(token).equals(TokenType.REFRESH);
    }

    public String createAccessToken(UserDetails userDetails, Role role) {
        Map<String, Object> claims = Map.ofEntries(
                Map.entry("type", TokenType.ACCESS),
                Map.entry("role", role)
        );
        return createToken(claims, userDetails, accessTokenExpiration);
    }

    public String createRefreshToken(UserDetails userDetails, Role role) {
        Map<String, Object> claims = Map.ofEntries(
                Map.entry("type", TokenType.REFRESH),
                Map.entry("role", role)
        );
        return createToken(claims, userDetails, refreshTokenExpiration);
    }
}

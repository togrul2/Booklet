package com.github.togrul2.booklet.services;

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

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
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
                // FIXME: fix wrong expiration.
                .claims(claims)
                .expiration(expirationDate)
                .signWith(getSecretKey())
                .compact();
    }

    public boolean isAccessTokenValid(String token) {
        return isTokenValid(token) && isAccessToken(token);
    }

    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token) && isRefreshToken(token);
    }

    /**
     * Check if the token is expired.
     *
     * @param token JWT token.
     * @return True if the token is not expired, false otherwise.
     */
    public boolean isTokenValid(String token) {
        return !extractExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    public String createAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = Map.of("type", TokenType.ACCESS);
        return createToken(claims, userDetails, accessTokenExpiration);
    }

    public boolean isAccessToken(String token) {
        return extractClaim(token, claims -> claims.get("type")).equals(TokenType.ACCESS);
    }

    public boolean isRefreshToken(String token) {
        return extractClaim(token, claims -> claims.get("type")).equals(TokenType.REFRESH);
    }

    public String createRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = Map.of("type", TokenType.REFRESH);
        return createToken(claims, userDetails, refreshTokenExpiration);
    }
}

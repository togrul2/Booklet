package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.entities.Role;
import com.github.togrul2.booklet.entities.Token;
import com.github.togrul2.booklet.entities.User;
import com.github.togrul2.booklet.repositories.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
@RequiredArgsConstructor
public class JwtService {
    private final TokenRepository tokenRepository;
    @Value("${spring.security.jwt.secret-key}")
    private String secretKey;
    @Value("${spring.security.jwt.access-expiration:300000}")
    private long accessTokenExpiration;  // In milliseconds.
    @Value("${spring.security.jwt.refresh-expiration:86400000}")
    private long refreshTokenExpiration;  // In milliseconds.

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

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
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

    private String createToken(HashMap<String, Object> claims, UserDetails userDetails, long expiration) {
        // To make the token unique, we use the current time in milliseconds.
        // This is done to prevent storing token uniqueness constraint in database.
        claims.put("uid", Instant.now().toEpochMilli());
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

    /**
     * Validates the refresh token. Fetches token from the database,
     * then checks if the token is a refresh token and if it is active.
     *
     * @param refreshToken The refresh token to validate.
     * @throws JwtException If the token is not a refresh token or if it is not active.
     */
    public void validateRefreshToken(String refreshToken) {
        final boolean isTokenActive = tokenRepository
                .findByToken(refreshToken)
                .map(Token::isActive)
                .orElse(false);

        if (!isRefreshToken(refreshToken) || !isTokenActive) {
            throw new JwtException("Bad refresh token");
        }
    }

    /**
     * Validates the refresh token. Checks if the token is a refresh token and if it is active.
     * This method should be used when the token entity is available to avoid repetitive database queries.
     *
     * @param token The refresh token to validate.
     * @throws JwtException If the token is not a refresh token or if it is not active.
     */
    private void validateRefreshToken(Token token) {
        if (!isRefreshToken(token.getToken()) || !token.isActive()) {
            throw new JwtException("Bad refresh token");
        }
    }

    public String createAccessToken(UserDetails userDetails, Role role) {
        HashMap<String, Object> claims = new HashMap<>(
                Map.ofEntries(
                        Map.entry("type", TokenType.ACCESS),
                        Map.entry("role", role)
                )
        );
        return createToken(claims, userDetails, accessTokenExpiration);
    }

    public String createRefreshToken(UserDetails userDetails, Role role) {
        HashMap<String, Object> claims = new HashMap<>(
                Map.ofEntries(
                        Map.entry("type", TokenType.REFRESH),
                        Map.entry("role", role)
                )
        );
        return createToken(claims, userDetails, refreshTokenExpiration);
    }

    public Token createAndStoreRefreshToken(User user, Role role) {
        String refreshToken = createRefreshToken(user, role);
        Token token = Token
                .builder()
                .user(user)
                .token(refreshToken)
                .active(true)
                .build();
        return tokenRepository.save(token);
    }

    /**
     * Validates and sets the matching token in the database as inactive so it cannot be used anymore.
     *
     * @param refreshToken The refresh token to set as inactive. Must be an active refresh token.
     * @throws JwtException If the token is not a refresh token or if it is not active.
     */
    public void deactivateRefreshToken(String refreshToken) {
        Token token = tokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> new JwtException("Bad refresh token"));

        validateRefreshToken(token);
        token.setActive(false);
        tokenRepository.save(token);
    }

    private enum TokenType {
        ACCESS, REFRESH
    }
}

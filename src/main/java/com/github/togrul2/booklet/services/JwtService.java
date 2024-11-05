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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class JwtService {
    private final TokenRepository tokenRepository;
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

    /**
     * Validates the refresh token. Checks if the token is a refresh token and if it is active.
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
     * Validates and sets the token as inactive so it cannot be used anymore.
     *
     * @param refreshToken The refresh token to set as inactive. Must be an active refresh token.
     * @throws JwtException If the token is not a refresh token or if it is not active.
     */
    public void deactivateRefreshToken(String refreshToken) {
        validateRefreshToken(refreshToken);
        tokenRepository.findByToken(refreshToken).ifPresent(
                token -> {
                    token.setActive(false);
                    tokenRepository.save(token);
                }
        );
    }
}

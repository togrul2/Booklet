package com.github.togrul2.booklet.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.togrul2.booklet.entities.Role;
import com.github.togrul2.booklet.services.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTH_PATH = "/api/v1/auth/";
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (
                request.getServletPath().startsWith(AUTH_PATH) ||
                        authHeader == null ||
                        !authHeader.startsWith("Bearer ")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwtToken = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwtToken);
            final Role role = jwtService.extractRole(jwtToken);
            final SecurityContext content = SecurityContextHolder.getContext();
            final Authentication authentication = content.getAuthentication();

            if (userEmail == null || authentication != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // This way we avoid hitting the database each time we need to authenticate a user,
            // since all necessary user info is in jwt token.
            UserDetails userDetails = User
                    .builder()
                    .username(userEmail)
                    .password("")
                    .authorities(role.getAuthorities())
                    .build();

            if (!jwtService.isAccessToken(jwtToken)) {
                filterChain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            content.setAuthentication(authToken);
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            // If the token is expired or invalid, we return an unauthenticated error response.
            formatJwtExceptionResponse(request, response, e);
        }
    }

    /**
     * Formats the error response to servlet response
     */
    private void formatJwtExceptionResponse(
            HttpServletRequest request, HttpServletResponse response, JwtException e
    ) throws IOException {
        PrintWriter out = response.getWriter();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(Map.of(
                "message", e.getMessage(),
                "status", HttpStatus.UNAUTHORIZED,
                "path", request.getRequestURI(),
                "timestamp", LocalDateTime.now().toString(),
                "error", HttpStatus.UNAUTHORIZED.getReasonPhrase()
        ));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        out.print(jsonString);
        out.flush();
    }
}

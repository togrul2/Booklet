package com.github.togrul2.booklet.filters;

import com.github.togrul2.booklet.entities.Role;
import com.github.togrul2.booklet.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTH_PATH = "/api/v1/auth";
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            if (request.getServletPath().contains(AUTH_PATH)) {
                return;
            }

            final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return;
            }

            final String jwtToken = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwtToken);
            final Role role = jwtService.extractRole(jwtToken);
            final SecurityContext content = SecurityContextHolder.getContext();
            final Authentication authentication = content.getAuthentication();

            if (userEmail == null || authentication != null) {
                return;
            }

            // This way we avoid hitting the database each time we need to authenticate a user,
            // since all necessary user info is in jwt token.
            UserDetails userDetails = User
                    .builder()
                    .username(userEmail)
                    .password("")
                    .roles(role.name())
                    .authorities(role.getAuthorities())
                    .build();

            if (jwtService.isAccessToken(jwtToken)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                content.setAuthentication(authToken);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            filterChain.doFilter(request, response);
        }
    }
}

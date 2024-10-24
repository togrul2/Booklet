package com.github.togrul2.booklet.configurations;

import com.github.togrul2.booklet.filters.JwtAuthenticationFilter;
import com.github.togrul2.booklet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    // Disable csrf for api and swagger. Enabled for mvc views.
    private final String[] IGNORED_CSRF_PATHS = {
            "/api/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html"
    };
    // Cors stuff.
    @Value("${spring.security.cors.allowed-origins:*}")
    private List<String> corsAllowedOrigins;
    @Value("${spring.security.cors.allowed-methods:*}")
    private List<String> corsAllowedMethods;
    @Value("${spring.security.cors.allowed-headers:*}")
    private List<String> corsAllowedHeaders;
    @Value("${spring.security.cors.allowed-origin-patterns:*}")
    private List<String> corsAllowedOriginPatterns;

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        return email -> userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public SecurityFilterChain filterChain(
            CorsConfigurationSource corsConfigurationSource,
            HttpSecurity httpSecurity,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {
        return httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.ignoringRequestMatchers(IGNORED_CSRF_PATHS))
                .authorizeHttpRequests(
                        request -> request
                                // Allow all requests to /api/v1/auth/ methods such as login or refresh.
                                .requestMatchers(
                                        "/api/v1/auth/**",
                                        "/actuator/**",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**"
                                )
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "api/v1/users")
                                .permitAll()
                                // Move to per method security.
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/authors",
                                        "/api/v1/authors/**",
                                        "/api/v1/books",
                                        "/api/v1/books/**",
                                        "/api/v1/genres",
                                        "/api/v1/genres/**"
                                )
                                .permitAll()
                                .requestMatchers(
                                        "/api/v1/authors",
                                        "/api/v1/authors/**",
                                        "/api/v1/books",
                                        "/api/v1/books/**",
                                        "/api/v1/genres",
                                        "/api/v1/genres/**"
                                )
                                .hasRole("ADMIN")
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/users",
                                        "/api/v1/users/**"
                                )
                                .hasRole("ADMIN")
                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final String corsUrlPattern = "/api/**";
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsAllowedOrigins);
        configuration.setAllowedMethods(corsAllowedMethods);
        configuration.setAllowedHeaders(corsAllowedHeaders);
        configuration.setAllowedOriginPatterns(corsAllowedOriginPatterns);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(corsUrlPattern, configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


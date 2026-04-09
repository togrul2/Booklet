package com.github.togrul2.booklet.clients;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationWebClient {
    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    private NotificationWebClient() {
        this.webClient = WebClient.builder().build();
        this.circuitBreaker = CircuitBreaker.ofDefaults("notificationService");
    }

    /**
     * Sends a notification to the notification service for a specific user.
     */
    public void sendNotification(long userID, String message) {
        webClient.post()
                .uri("http://notification-service/notify")
                .bodyValue(Map.of("userID", userID, "message", message))
                .retrieve()
                .bodyToMono(Void.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .subscribe();
    }
}

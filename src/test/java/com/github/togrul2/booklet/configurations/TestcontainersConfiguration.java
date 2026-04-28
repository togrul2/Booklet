package com.github.togrul2.booklet.configurations;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {
    @Bean
    @ServiceConnection
    @SuppressWarnings("resource")
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres:15").withReuse(true);
    }

    // TODO: Add redis container.
}

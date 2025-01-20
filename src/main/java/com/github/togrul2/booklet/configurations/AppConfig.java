package com.github.togrul2.booklet.configurations;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * Application configuration class.
 * Contains beans for OpenAPI configuration and spring web support for paginated responses.
 *
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class AppConfig {
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    @Bean
    public OpenAPI openAPI() {
        Contact contact = new Contact()
                                .name("Toghrul Asadov")
                                .email("toghrul.asadov.28@gmail.com")
                                .url("https://github.com/togrul2");
        License license = new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0");
        Info info = new Info()
                .title("Booklet API")
                .description("API for Booklet application.")
                .version("1.0")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("JWT Authentication"))
                .components(new Components().addSecuritySchemes("JWT Authentication", createAPIKeyScheme()))
                .info(info);
    }
}

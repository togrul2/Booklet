package com.github.togrul2.booklet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class BookletApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookletApplication.class, args);
    }
}

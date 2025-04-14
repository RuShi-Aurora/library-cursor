package com.example.cursorlibrary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.cursorlibrary.repository")
public class CursorLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(CursorLibraryApplication.class, args);
    }

}

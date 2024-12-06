package com.chensoul.oauth2client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class TestcontainersTests {

    @Container
    static GenericContainer<?> authServer = new GenericContainer<>("chensoul/spring-authorization-server:0.0.1")
            .withExposedPorts(9000);

    @Test
    void contextLoads() {
    }

    @DynamicPropertySource
    static void clientRegistrationProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.client.provider.spring-authorization-server.issuer-uri",
                () -> "http://localhost:" + authServer.getExposedPorts().get(0));
    }
}
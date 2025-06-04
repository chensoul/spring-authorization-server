package com.chensoul.oauth2client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
public class OAuth2ClientApplicationTest {
    @Container
    static GenericContainer<?> authServer = new GenericContainer<>("chensoul/spring-authorization-server:0.0.1")
            .withExposedPorts(9000);

    @Value("${spring.security.oauth2.client.provider.spring-authorization-server.issuer-uri}")
    String issuerUri;

    @DynamicPropertySource
    static void clientRegistrationProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.client.provider.spring-authorization-server.issuer-uri",
                () -> "http://localhost:" + authServer.getFirstMappedPort());
    }

    @Test
    void authorizationServerAccessOpenIdConfiguration() {
        String oidcMetadataUrl = issuerUri + "/.well-known/openid-configuration";
        RestClient restClient = RestClient.create();
        // @formatter:off
        ResponseEntity<String> result = restClient.get()
                .uri(oidcMetadataUrl)
                .retrieve()
                .toEntity(String.class);
        // @formatter:on
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}

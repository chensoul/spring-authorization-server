package com.chensoul.oauth2client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.experimental.boot.server.exec.CommonsExecWebServerFactoryBean;
import org.springframework.experimental.boot.test.context.EnableDynamicProperty;
import org.springframework.experimental.boot.test.context.OAuth2ClientProviderIssuerUri;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = OAuth2ClientMainTest.TestOAuth2ClientConfig.class)
class OAuth2ClientMainTest {
    @Value("${spring.security.oauth2.client.provider.spring-authorization-server.issuer-uri}")
    String issuerUri;

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

    @TestConfiguration(proxyBeanMethods = false)
    @EnableDynamicProperty
    static class TestOAuth2ClientConfig {
        @Bean
        @OAuth2ClientProviderIssuerUri(providerName = "spring-authorization-server")
        static CommonsExecWebServerFactoryBean authorizationServer() {
            return CommonsExecWebServerFactoryBean.builder()
                    .classpath(cp -> cp.files("spring-authorization-server-0.0.1-SNAPSHOT.jar"))
                    .mainClass("org.springframework.boot.loader.launch.JarLauncher");
        }
    }
}
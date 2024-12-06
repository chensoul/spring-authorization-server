package com.chensoul.authserver.configuration;

import com.chensoul.authserver.authentication.CustomUserDetailsService;
import com.chensoul.authserver.oauth2.ScopeAndRedirectUriValidationPostProcessor;
import com.chensoul.authserver.oauth2.client.ClientProperties;
import com.chensoul.authserver.oauth2.client.CustomRegisteredClient;
import com.chensoul.authserver.oauth2.client.CustomRegisteredClientRepository;
import com.chensoul.authserver.oauth2.token.IdTokenAddressCustomizer;
import com.chensoul.authserver.oauth2.token.IdTokenCustomClaimsCustomizer;
import com.chensoul.authserver.oauth2.token.IdTokenEmailCustomizer;
import com.chensoul.authserver.oauth2.token.IdTokenPhoneNumberCustomizer;
import com.chensoul.authserver.oauth2.token.IdTokenProfileCustomizer;
import com.chensoul.authserver.oauth2.token.TokenCustomizer;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.DispatcherType;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, CustomRegisteredClientRepository repository, OAuth2AuthorizationService authorizationService) throws Exception {
        OAuth2AuthorizationServerConfigurer authServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        return http.authorizeHttpRequests(req -> {
            req.requestMatchers("/", "/style.css", "/favicon.ico").permitAll()
                    .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                    .anyRequest().authenticated();
        }).csrf(csrf -> {
            csrf.ignoringRequestMatchers(authServerConfigurer.getEndpointsMatcher());
        }).formLogin(login -> {
            login.loginPage("/login").defaultSuccessUrl("/").permitAll();
        }).logout(logout -> {
            logout.logoutSuccessUrl("/");
        }).cors(cors -> {
            cors.configurationSource(allowAllCorsConfiguration());
        }).with(authServerConfigurer, authServer -> {
            authServer.oidc(Customizer.withDefaults())
                    .withObjectPostProcessor(new ScopeAndRedirectUriValidationPostProcessor(repository, authorizationService));
        }).oauth2ResourceServer(resource -> {
            resource.jwt(Customizer.withDefaults());
        }).build();
    }

    @Bean
    CustomRegisteredClientRepository registeredClientRepository(ServerProperties properties) {
        if (properties.getClients().isEmpty()) {
            return new CustomRegisteredClientRepository(Defaults.CLIENT);
        } else {
            List<CustomRegisteredClient> registeredClients = properties.getClients().stream().map(SecurityConfiguration::toRegisteredClient).toList();
            return new CustomRegisteredClientRepository(registeredClients);
        }
    }

    @Bean
    OAuth2AuthorizationService authorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }

    @Bean
    JWKSource<SecurityContext> jwkSource(ServerProperties properties) {
        RSAKey rsaKey = RsaKeys.getRsaKey(properties.getJwk().random(), properties.getJwk().publicKey(), properties.getJwk().privateKey());
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet(jwkSet);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return Defaults.PASSWORD_ENCODER;
    }

    @Bean
    CustomUserDetailsService userDetailsService(ServerProperties properties) {
        return properties.getUsers().isEmpty() ? new CustomUserDetailsService(Defaults.USER):new CustomUserDetailsService(properties.getUsers());
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        List<TokenCustomizer> tokenCustomizers = Arrays.asList(
                new IdTokenEmailCustomizer(),
                new IdTokenProfileCustomizer(),
                new IdTokenPhoneNumberCustomizer(),
                new IdTokenAddressCustomizer(),
                new IdTokenCustomClaimsCustomizer()
        );
        return (context) -> tokenCustomizers.forEach(customizer -> customizer.customize(context));
    }

    private static CustomRegisteredClient toRegisteredClient(ClientProperties properties) {
        return CustomRegisteredClient.withId(properties.getClientId())
                .clientId(properties.getClientId()).clientSecret(properties.getClientSecret()).scopes(s -> {
                    s.addAll(properties.getScope());
                }).clientAuthenticationMethods(m -> {
                    m.addAll(properties.getClientAuthenticationMethods());
                }).authorizationGrantTypes(a -> {
                    a.addAll(properties.getAuthorizationGrantTypes());
                }).redirectUris(r -> {
                    r.addAll(properties.getRedirectUris());
                }).postLogoutRedirectUris(r -> {
                    r.addAll(properties.getPostLogoutRedirectUris());
                }).clientSettings(
                        ClientSettings.builder()
                                .requireAuthorizationConsent(properties.isRequireAuthorizationConsent())
                                .requireProofKey(properties.isRequireProofKey())
                                .build()
                ).tokenSettings(
                        TokenSettings.builder()
                                .accessTokenFormat(new OAuth2TokenFormat(properties.getToken().getAccessTokenFormat()))
                                .accessTokenTimeToLive(properties.getToken().getAccessTokenTimeToLive())
                                .refreshTokenTimeToLive(properties.getToken().getRefreshTokenTimeToLive())
                                .idTokenSignatureAlgorithm(SignatureAlgorithm.from(properties.getToken().getIdTokenSignatureAlgorithm()))
                                .authorizationCodeTimeToLive(properties.getToken().getAuthorizationCodeTimeToLive())
                                .reuseRefreshTokens(properties.getToken().isReuseRefreshTokens())
                                .deviceCodeTimeToLive(properties.getToken().getDeviceCodeTimeToLive())
                                .build()
                )
                .validateScope(properties.isValidateScope())
                .validateRedirectUri(properties.isValidateRedirectUri())
                .build();
    }

    private static CorsConfigurationSource allowAllCorsConfiguration() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        return httpServletRequest -> configuration;
    }
}

package com.chensoul.authserver.oauth2;

import com.chensoul.authserver.oauth2.client.CustomRegisteredClientRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationProvider;

public class ScopeAndRedirectUriValidationPostProcessor implements ObjectPostProcessor<AuthenticationProvider> {
    private final CustomRegisteredClientRepository repository;
    private final OAuth2AuthorizationService authorizationService;

    public ScopeAndRedirectUriValidationPostProcessor(CustomRegisteredClientRepository repository, OAuth2AuthorizationService authorizationService) {
        this.repository = repository;
        this.authorizationService = authorizationService;
    }

    public AuthenticationProvider postProcess(AuthenticationProvider authenticationProvider) {
        if (authenticationProvider instanceof OAuth2AuthorizationCodeRequestAuthenticationProvider provider) {
            provider.setAuthenticationValidator(new AuthorizationCodeRequestValidator());
        } else if (authenticationProvider instanceof OAuth2ClientCredentialsAuthenticationProvider provider) {
            provider.setAuthenticationValidator(new ClientCredentialsRequestValidator());
        } else if (authenticationProvider instanceof org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationProvider provider) {
            return new OidcLogoutAuthenticationProvider(provider, this.authorizationService, this.repository);
        }

        return authenticationProvider;
    }
}

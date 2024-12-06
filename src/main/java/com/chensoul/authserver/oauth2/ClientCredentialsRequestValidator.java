package com.chensoul.authserver.oauth2;

import com.chensoul.authserver.oauth2.client.CustomRegisteredClient;
import java.util.function.Consumer;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationValidator;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

public class ClientCredentialsRequestValidator implements Consumer<OAuth2ClientCredentialsAuthenticationContext> {
    public ClientCredentialsRequestValidator() {
    }

    public void accept(OAuth2ClientCredentialsAuthenticationContext context) {
        RegisteredClient registeredClient = context.getRegisteredClient();
        if (registeredClient instanceof CustomRegisteredClient client) {
            if (client.isValidateScope()) {
                OAuth2ClientCredentialsAuthenticationValidator.DEFAULT_SCOPE_VALIDATOR.accept(context);
            }

        } else {
            throw new RuntimeException("Invalid client type: " + context.getRegisteredClient().getClass());
        }
    }
}
package com.chensoul.authserver.configuration;

import com.chensoul.authserver.authentication.CustomUser;
import com.chensoul.authserver.oauth2.client.CustomRegisteredClient;
import java.util.Map;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

public final class Defaults {
    public static final CustomRegisteredClient CLIENT;
    public static final CustomUser USER;
    public static final PasswordEncoder PASSWORD_ENCODER;

    private Defaults() {
    }

    static {
        CLIENT = CustomRegisteredClient
                .withId("default-client-id")
                .clientId("default-client-id")
                .clientSecret("default-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .redirectUri("http://localhost:8080")
                .scope("openid")
                .scope("email")
                .scope("profile")
                .validateRedirectUri(false)
                .build();

        USER = new CustomUser("user", "password", Map.of("email", "user@example.com"));

        PASSWORD_ENCODER = NoOpPasswordEncoder.getInstance();
    }
}

package com.chensoul.authserver.config;

import com.chensoul.authserver.authentication.CustomUser;
import com.chensoul.authserver.oauth2.client.ClientProperties;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.lang.Nullable;

@ConfigurationProperties(prefix = "application.spring-authorization-server")
public class ServerProperties {
    @Nullable
    private final List<CustomUser> users;
    @Nullable
    private final List<ClientProperties> clients;
    @Nullable
    private final Jwk jwk;

    @ConstructorBinding
    public ServerProperties(@Nullable List<CustomUser> users, @Nullable List<ClientProperties> clients, @Nullable Jwk jwk) {
        this.users = users;
        this.clients = clients;
        this.jwk = jwk;
    }

    public List<CustomUser> getUsers() {
        return this.users!=null ? this.users:Arrays.asList(Defaults.USER);
    }

    public List<ClientProperties> getClients() {
        return this.clients!=null ? this.clients:Collections.emptyList();
    }

    public Jwk getJwk() {
        return this.jwk!=null ? this.jwk:new Jwk(true, null, null);
    }

    public record Jwk(boolean random, RSAPublicKey publicKey, RSAPrivateKey privateKey) {
    }
}

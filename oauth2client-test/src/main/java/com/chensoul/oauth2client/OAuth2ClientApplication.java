package com.chensoul.oauth2client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

@SpringBootApplication
public class OAuth2ClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(OAuth2ClientApplication.class, args);
    }

    @Bean
    OidcUserService oidcUserService() {
        var oidcUserService = new OidcUserService();
        oidcUserService.setOidcUserMapper((oidcUserRequest, oidcUserInfo) -> {
            // Will map the "roles" claim from the `id_token` into user authorities (roles)
            var roles = oidcUserRequest.getIdToken().getClaimAsStringList("roles");
            var authorities = AuthorityUtils.createAuthorityList();
            if (roles!=null) {
                roles.stream()
                        .map(r -> "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }
            return new DefaultOidcUser(authorities, oidcUserRequest.getIdToken(), oidcUserInfo);
        });
        return oidcUserService;
    }
}

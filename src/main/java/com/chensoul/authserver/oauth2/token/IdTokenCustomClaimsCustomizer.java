package com.chensoul.authserver.oauth2.token;

import com.chensoul.authserver.authentication.CustomUser;
import com.chensoul.authserver.authentication.UserAttributesClaimAccessor;
import java.util.Map;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

public class IdTokenCustomClaimsCustomizer extends TokenCustomizer {
    public IdTokenCustomClaimsCustomizer() {
    }

    public boolean shouldCustomize(JwtEncodingContext context) {
        return this.isIdToken(context) && context.getAuthorizedScopes().contains(OidcScopes.OPENID);
    }

    public void customizeInternal(JwtEncodingContext context) {
        this.getPrincipal(context)
                .map(CustomUser::getTokenClaims)
                .map(UserAttributesClaimAccessor::getCustomClaims)
                .orElse(Map.of())
                .forEach((key, value) -> context.getClaims().claim(key, value));
    }
}

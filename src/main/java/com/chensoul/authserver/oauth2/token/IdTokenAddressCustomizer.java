package com.chensoul.authserver.oauth2.token;

import com.chensoul.authserver.authentication.UserAttributesClaimAccessor;
import com.chensoul.authserver.authentication.CustomUser;
import java.util.Map;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

public class IdTokenAddressCustomizer extends TokenCustomizer {
    public IdTokenAddressCustomizer() {
    }

    boolean shouldCustomize(JwtEncodingContext context) {
        return this.isIdToken(context) && this.hasScope(context, "address");
    }

    void customizeInternal(JwtEncodingContext context) {
        this.getPrincipal(context)
                .map(CustomUser::getTokenClaims)
                .map(UserAttributesClaimAccessor::getOidcAddressClaims)
                .orElse(Map.of())
                .forEach((key, value) -> context.getClaims().claim(key, value));
    }
}
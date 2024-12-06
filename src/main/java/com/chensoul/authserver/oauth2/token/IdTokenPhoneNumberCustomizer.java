package com.chensoul.authserver.oauth2.token;

import com.chensoul.authserver.authentication.UserAttributesClaimAccessor;
import com.chensoul.authserver.authentication.CustomUser;
import java.util.Map;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

public class IdTokenPhoneNumberCustomizer extends TokenCustomizer {
    public IdTokenPhoneNumberCustomizer() {
    }

    boolean shouldCustomize(JwtEncodingContext context) {
        return this.isIdToken(context) && this.hasScope(context, "phone");
    }

    void customizeInternal(JwtEncodingContext context) {
        this.getPrincipal(context)
                .map(CustomUser::getTokenClaims)
                .map(UserAttributesClaimAccessor::getOidcPhoneClaims)
                .orElse(Map.of())
                .forEach((key, value) -> context.getClaims().claim(key, value));
    }
}

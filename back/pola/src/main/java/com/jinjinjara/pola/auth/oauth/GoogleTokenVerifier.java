package com.jinjinjara.pola.auth.oauth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class GoogleTokenVerifier {

    private static final List<String> VALID_ISSUERS = List.of(
            "https://accounts.google.com",
            "accounts.google.com"
    );

    private final GoogleIdTokenVerifier verifier;
    private final List<String> allowedClientIds;

    public GoogleTokenVerifier(@Value("${google.client-ids}") String clientIdsCsv) {
        this.allowedClientIds = Arrays.stream(clientIdsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        // 서명/만료 검증
        this.verifier = new GoogleIdTokenVerifier
                .Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .build();
    }

    public GoogleIdToken.Payload verify(String idToken) throws Exception {
        GoogleIdToken token = verifier.verify(idToken);
        if (token == null) {
            throw new IllegalArgumentException("Invalid Google ID token (signature/expiry).");
        }

        GoogleIdToken.Payload payload = token.getPayload();

        // issuer 검증
        String issuer = payload.getIssuer();
        if (!VALID_ISSUERS.contains(issuer)) {
            throw new IllegalArgumentException("Invalid issuer: " + issuer);
        }

        // audience(any-match) 검증
        String aud = payload.getAudience() == null ? null : payload.getAudience().toString();
        boolean audOk = payload.getAudienceAsList().stream()
                .anyMatch(allowedClientIds::contains);
        if (!audOk) {
            throw new IllegalArgumentException("Audience not allowed: " + aud);
        }

        // email_verified 권장 체크
        Object ev = payload.get("email_verified");
        if (ev instanceof Boolean evb && !evb) {
            throw new IllegalArgumentException("Email is not verified.");
        }

        return payload;
    }
}
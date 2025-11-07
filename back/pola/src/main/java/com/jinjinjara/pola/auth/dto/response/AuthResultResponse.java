package com.jinjinjara.pola.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResultResponse {
    private final TokenResponse tokenResponse;
    private final boolean isNewUser;
}

package com.jinjinjara.pola.auth.dto.common;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenDto {
    private String grantType;
    private String accessToken;
    private long   accessTokenExpiresIn;
    private String refreshToken;
}

package com.jinjinjara.pola.dto.response;

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

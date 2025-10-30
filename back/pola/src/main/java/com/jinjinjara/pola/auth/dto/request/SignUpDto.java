package com.jinjinjara.pola.auth.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignUpDto {
    private String email;
    private String username;
}

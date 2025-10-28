package com.jinjinjara.pola.dto.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Authority {
    ROLE_GUEST,
    ROLE_MEMBER,
    ROLE_ADMIN
}
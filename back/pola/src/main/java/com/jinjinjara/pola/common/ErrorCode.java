package com.jinjinjara.pola.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 유저
    USER_NOT_FOUND("USER-001", "유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 데이터
    DATA_NOT_FOUND("DATA-001", "데이터를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;

}

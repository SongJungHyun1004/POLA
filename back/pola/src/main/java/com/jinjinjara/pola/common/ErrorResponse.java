package com.jinjinjara.pola.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse<T> {
    private final String errorCode;
    private final String message;

    public static ErrorResponse res(final CustomException customException) {
        String errorCode = customException.getErrorCode().getCode();
        String message = customException.getErrorCode().getMessage();
        return new ErrorResponse(errorCode, message);
    }

    public static ErrorResponse res(final String errorCode, final Exception exception) {
        return new ErrorResponse(errorCode, exception.getMessage());
    }
}

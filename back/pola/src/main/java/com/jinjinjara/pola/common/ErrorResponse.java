package com.jinjinjara.pola.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.FieldError;

import java.util.List;

@Getter
@Builder
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

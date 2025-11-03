package com.jinjinjara.pola.common;

import lombok.*;
import com.fasterxml.jackson.annotation.*;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @JsonIgnore
    private boolean success;

    @JsonIgnore
    private ErrorResponse error;

    @JsonProperty("data")
    private T data;

    @JsonProperty("message")
    private String message;

    @JsonProperty("status")
    public String getStatusView() {
        return success ? "SUCCESS" : "FAIL";
    }

    @JsonProperty("code")
    public String getCodeView() {
        return (error != null) ? error.getErrorCode() : null;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static ApiResponse<List<Object>> okMessage(String message) {
        return ApiResponse.<List<Object>>builder()
                .success(true)
                .message(message)
                .data(Collections.emptyList())
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .success(true)
                .message("성공했습니다.")
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(new ErrorResponse(code, message))
                .build();
    }
}

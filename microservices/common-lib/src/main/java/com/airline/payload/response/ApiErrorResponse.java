package com.airline.payload.response;

import com.airline.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    private int status;
    private String code;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private List<FieldError> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }

    // ===== HELPERS ESTÁTICOS =====

    public static ApiErrorResponse of(
            int status,
            ErrorCode code,
            String message,
            String path
    ) {
        return ApiErrorResponse.builder()
                .status(status)
                .code(code.name())
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .errors(Collections.emptyList())
                .build();
    }

    public static ApiErrorResponse of(
            int status,
            ErrorCode code,
            String message,
            String path,
            List<FieldError> errors
    ) {
        return ApiErrorResponse.builder()
                .status(status)
                .code(code.name())
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
    }

}
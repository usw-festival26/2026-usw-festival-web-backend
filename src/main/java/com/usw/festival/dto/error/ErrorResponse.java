package com.usw.festival.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String code,
        String message,
        String path,
        OffsetDateTime timestamp,
        List<FieldErrorDetail> fieldErrors
) {
    public static ErrorResponse of(int status, String code, String message, String path) {
        return new ErrorResponse(
                status,
                code,
                message,
                path,
                OffsetDateTime.now(),
                null
        );
    }

    public static ErrorResponse of(int status, String code, String message, String path,
                                   List<FieldErrorDetail> fieldErrors) {
        return new ErrorResponse(
                status,
                code,
                message,
                path,
                OffsetDateTime.now(),
                fieldErrors
        );
    }

    public record FieldErrorDetail(
            String field,
            String message
    ) {
    }
}

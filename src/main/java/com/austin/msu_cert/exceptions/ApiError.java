package com.austin.msu_cert.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiError {

    private int status;
    private String error;
    private String message;
    private String path;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static ApiError of(int status, String error, String message, String path) {
        return ApiError.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

    public static ApiError badRequest(String message, String path) {
        return of(400, "Bad Request", message, path);
    }

    public static ApiError unauthorized(String message, String path) {
        return of(401, "Unauthorized", message, path);
    }

    public static ApiError forbidden(String message, String path) {
        return of(403, "Forbidden", message, path);
    }

    public static ApiError notFound(String message, String path) {
        return of(404, "Not Found", message, path);
    }

    public static ApiError internalError(String message, String path) {
        return of(500, "Internal Server Error", message, path);
    }
}
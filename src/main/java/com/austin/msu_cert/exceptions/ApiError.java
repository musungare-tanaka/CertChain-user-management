package com.austin.msu_cert.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomError {

     int status;

     String error;

     String message;

     String path;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
     LocalDateTime timestamp = LocalDateTime.now();

    public static CustomError of(int status, String error, String message, String path) {
        return CustomError.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

    public static CustomError badRequest(String message, String path) {
        return of(400, "Bad Request", message, path);
    }

    public static CustomError unauthorized(String message, String path) {
        return of(401, "Unauthorized", message, path);
    }

    public static CustomError forbidden(String message, String path) {
        return of(403, "Forbidden", message, path);
    }

    public static CustomError notFound(String message, String path) {
        return of(404, "Not Found", message, path);
    }

    public static CustomError internalError(String message, String path) {
        return of(500, "Internal Server Error", message, path);
    }
}
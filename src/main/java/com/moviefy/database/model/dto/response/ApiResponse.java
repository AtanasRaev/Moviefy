package com.moviefy.database.model.dto.response;

import java.time.Instant;

public class ApiResponse<T> {

    private final boolean success;
    private final int status;
    private final String message;
    private final T data;
    private final Instant timestamp = Instant.now();

    private ApiResponse(boolean success, int status, String message, T data) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(true, status, message, data);
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(false, status, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}

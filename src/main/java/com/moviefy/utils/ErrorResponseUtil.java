package com.moviefy.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.Map;

public class ErrorResponseUtil {
    public static ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("error", error);
        response.put("message", message);

        return ResponseEntity
                .status(status)
                .body(response);
    }
}

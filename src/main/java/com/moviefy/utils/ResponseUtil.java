package com.moviefy.utils;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

public class ResponseUtil {
    public static ResponseEntity<Map<String, Object>> getMapResponseEntity(@PathVariable String mediaType, Page<?> mediaPage) {
        if (mediaPage == null || mediaPage.isEmpty()) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "Resource not found",
                    String.format("There are no %s", mediaType)
            );
        }

        Map<String, Object> response = Map.of(
                "items_on_page", mediaPage.getNumberOfElements(),
                "total_items", mediaPage.getTotalElements(),
                "total_pages", mediaPage.getTotalPages(),
                "current_page", mediaPage.getNumber() + 1,
                mediaType, mediaPage.getContent()
        );

        return ResponseEntity.ok(response);
    }
}

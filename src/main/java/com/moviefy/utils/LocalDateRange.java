package com.moviefy.utils;

import java.time.LocalDate;
import java.util.Objects;

public record LocalDateRange(LocalDate start, LocalDate end) {
    public LocalDateRange {
        Objects.requireNonNull(start, "start date cannot be null");
        Objects.requireNonNull(end, "end date cannot be null");
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }
}
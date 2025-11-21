package com.moviefy.utils;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TvSeriesTypesNormalizationUtil {
    private static final List<String> LOWERED_TYPES = List.of("scripted", "miniseries", "reality", "documentary");

    public List<String> processTypes(List<String> types) {
        if (types == null || types.isEmpty()) {
            types = LOWERED_TYPES;
        } else {
            types = types.stream()
                    .map(String::toLowerCase)
                    .toList();
        }
        return types;
    }
}

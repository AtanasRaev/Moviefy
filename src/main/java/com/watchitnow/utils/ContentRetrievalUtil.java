package com.watchitnow.utils;

import com.watchitnow.database.model.entity.Movie;
import com.watchitnow.database.model.entity.TvSeries;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

@Component
public class ContentRetrievalUtil {
    private static final int MAX_EMPTY_MONTHS = 12;
    private static final int DAYS_OFFSET = 7;
    private static final int MAX_ITEMS = 100;

    public <T, R> Page<R> fetchContentFromDateRange(Pageable pageable,
                                                          Function<LocalDateRange, List<T>> fetchFunction,
                                                          Function<T, R> mapFunction) {
        List<R> allContent = new ArrayList<>();
        LocalDate currentDate = LocalDate.now().minusDays(DAYS_OFFSET);
        int emptyCount = 0;

        while (emptyCount < MAX_EMPTY_MONTHS && allContent.size() < MAX_ITEMS) {
            LocalDate endDate = currentDate.withDayOfMonth(1);
            LocalDateRange dateRange = new LocalDateRange(endDate, currentDate);

            List<T> fetchedContent = fetchFunction.apply(dateRange);
            fetchedContent.sort(Comparator.comparing(this::getDate).reversed());

            List<R> mappedContent = fetchedContent.stream()
                    .map(mapFunction)
                    .limit(MAX_ITEMS - allContent.size())
                    .toList();

            if (mappedContent.isEmpty()) {
                emptyCount++;
            } else {
                allContent.addAll(mappedContent);
            }

            if (hasEnoughContentForPage(allContent.size(), pageable)) {
                break;
            }

            currentDate = endDate.minusDays(1);
        }

        return createPageFromContent(allContent, pageable);
    }

    private boolean hasEnoughContentForPage(int contentSize, Pageable pageable) {
        return contentSize >= (pageable.getPageNumber() + 1) * pageable.getPageSize() ||
                contentSize >= MAX_ITEMS;
    }

    private <R> Page<R> createPageFromContent(List<R> content, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(Math.min(start + pageable.getPageSize(), content.size()), MAX_ITEMS);

        List<R> pageContent = start < end
                ? content.subList(start, end)
                : Collections.emptyList();

        return new PageImpl<>(
                pageContent,
                pageable,
                Math.min(content.size(), MAX_ITEMS)
        );
    }

    private LocalDate getDate(Object content) {
        if (content instanceof Movie) {
            return ((Movie) content).getReleaseDate();
        } else if (content instanceof TvSeries) {
            return ((TvSeries) content).getFirstAirDate();
        }
        throw new IllegalArgumentException("Unknown content type: " + content.getClass());
    }
}
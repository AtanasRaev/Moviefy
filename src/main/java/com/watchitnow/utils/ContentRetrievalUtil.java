package com.watchitnow.utils;

import com.watchitnow.database.model.entity.Movie;
import com.watchitnow.database.model.entity.TvSeries;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ContentRetrievalUtil {
    public <T, R> Set<R> fetchContentFromDateRange(int targetCount,
                                                   Function<LocalDateRange, List<T>> fetchFunction,
                                                   Function<T, R> mapFunction,
                                                   Function<R, String> posterPathExtractor) {
        Set<R> content = new LinkedHashSet<>();
        //TODO: Decide from which date to start(consider that there is not video player for each movie/tv-series)
        LocalDate currentDate = LocalDate.now().minusDays(7);
        int emptyCount = 0;

        while (content.size() < targetCount) {
            LocalDate endDate = currentDate.withDayOfMonth(1);
            LocalDateRange dateRange = new LocalDateRange(endDate, currentDate);

            List<T> fetchedContent = fetchFunction.apply(dateRange);
            fetchedContent.sort(Comparator.comparing(this::getDate).reversed());

            List<R> mappedContent = fetchedContent.stream()
                    .map(mapFunction)
                    .toList();

            mappedContent = mappedContent.stream()
                    .filter(item -> posterPathExtractor.apply(item) != null && !posterPathExtractor.apply(item).isEmpty())
                    .toList();

            if (mappedContent.isEmpty()) {
                emptyCount++;
            }

            if (emptyCount == 12) {
                break;
            }

            content.addAll(mappedContent);
            currentDate = endDate.minusDays(1);
        }

        return content.stream().limit(targetCount).collect(Collectors.toSet());
    }

    private LocalDate getDate(Object content) {
        if (content instanceof Movie) {
            return ((Movie) content).getReleaseDate();
        } else if (content instanceof TvSeries) {
            return ((TvSeries) content).getFirstAirDate();
        }
        throw new IllegalArgumentException("Unknown content type");
    }
}

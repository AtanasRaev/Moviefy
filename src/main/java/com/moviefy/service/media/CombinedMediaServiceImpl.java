package com.moviefy.service.media;

import com.moviefy.database.model.dto.pageDto.CombinedMediaProjection;
import com.moviefy.database.model.dto.pageDto.SearchResultPageDTO;
import com.moviefy.database.repository.media.CombinedMediaRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CombinedMediaServiceImpl implements CombinedMediaService {

    private final CombinedMediaRepository combinedMediaRepository;

    public CombinedMediaServiceImpl(CombinedMediaRepository combinedMediaRepository) {
        this.combinedMediaRepository = combinedMediaRepository;
    }

    @Override
    @Cacheable(
            cacheNames = "combinedByGenres",
            unless = "#result == null || #result['results'] == null || #result['results'].isEmpty()"
    )
    public Map<String, Object> getCombinedMediaByGenres(List<String> genres, int page, int size) {
        int offset = (page - 1) * size;

        List<String> lowerCaseSeriesGenres = new ArrayList<>(genres.stream().map(String::toLowerCase).toList());
        List<String> lowerCaseMoviesGenres = genres.stream()
                .map(String::toLowerCase)
                .toList();

        if (lowerCaseSeriesGenres.contains("action") || lowerCaseSeriesGenres.contains("adventure")) {
            lowerCaseSeriesGenres.remove("action");
            lowerCaseSeriesGenres.remove("adventure");
            lowerCaseSeriesGenres.add("action & adventure");
        }

        if (lowerCaseSeriesGenres.contains("science fiction") || lowerCaseSeriesGenres.contains("fantasy")) {
            lowerCaseSeriesGenres.remove("science fiction");
            lowerCaseSeriesGenres.remove("fantasy");
            lowerCaseSeriesGenres.add("sci-fi & fantasy");
        }

        if (lowerCaseSeriesGenres.contains("war") || lowerCaseSeriesGenres.contains("politics")) {
            lowerCaseSeriesGenres.remove("war");
            lowerCaseSeriesGenres.remove("politics");
            lowerCaseSeriesGenres.add("war & politics");
        }

        List<CombinedMediaProjection> combinedRows =
                combinedMediaRepository.findCombinedByGenres(lowerCaseMoviesGenres, lowerCaseSeriesGenres, size, offset);

        long totalItems = combinedMediaRepository.countCombinedByGenres(lowerCaseMoviesGenres, lowerCaseSeriesGenres);
        int totalPages = (size > 0) ? (int) Math.ceil((double) totalItems / (double) size) : 0;

        List<SearchResultPageDTO> results = combinedRows.stream().map(row -> {
            SearchResultPageDTO dto = new SearchResultPageDTO();
            dto.setId(row.getId());
            dto.setApiId(row.getApi_id());
            dto.setType(row.getType());
            dto.setTitle(row.getTitle());
            dto.setPosterPath(row.getPosterPath());
            dto.setVoteAverage(row.getVoteAverage());
            dto.setYear(row.getYear());
            dto.setSeasonsCount(row.getSeasonsCount());
            dto.setEpisodesCount(row.getEpisodesCount());
            dto.setRuntime(row.getRuntime());
            return dto;
        }).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items_on_page", results.size());
        response.put("current_page", page);
        response.put("total_items", totalItems);
        response.put("total_pages", totalPages);
        response.put("results", results);

        return response;
    }
}

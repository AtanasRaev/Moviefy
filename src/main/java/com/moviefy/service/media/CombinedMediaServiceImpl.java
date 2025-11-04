package com.moviefy.service.media;

import com.moviefy.database.model.dto.pageDto.CombinedMediaProjection;
import com.moviefy.database.model.dto.pageDto.SearchResultDTO;
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

        List<String> seriesGenres = new ArrayList<>(genres);
        Set<String> lowerCaseGenres = new HashSet<>();
        for (String g : genres) {
            if (g != null) lowerCaseGenres.add(g.toLowerCase());
        }
        if (lowerCaseGenres.contains("action") || lowerCaseGenres.contains("adventure")) {
            seriesGenres.removeIf(g -> g != null && (g.equalsIgnoreCase("action") || g.equalsIgnoreCase("adventure")));
            seriesGenres.add("Action & Adventure");
        }
        if (lowerCaseGenres.contains("science fiction") || lowerCaseGenres.contains("fantasy")) {
            seriesGenres.removeIf(g -> g != null && (g.equalsIgnoreCase("Science Fiction") || g.equalsIgnoreCase("fantasy")));
            seriesGenres.add("Sci-Fi & Fantasy");
        }
        if (lowerCaseGenres.contains("war") || lowerCaseGenres.contains("politics")) {
            seriesGenres.removeIf(g -> g != null && (g.equalsIgnoreCase("war") || g.equalsIgnoreCase("politics")));
            seriesGenres.add("War & Politics");
        }

        List<CombinedMediaProjection> combinedRows =
                combinedMediaRepository.findCombinedByGenres(genres, seriesGenres, size, offset);

        long totalItems = combinedMediaRepository.countCombinedByGenres(genres, seriesGenres);
        int totalPages = (size > 0) ? (int) Math.ceil((double) totalItems / (double) size) : 0;

        List<SearchResultDTO> results = combinedRows.stream().map(row -> {
            SearchResultDTO dto = new SearchResultDTO();
            dto.setId(row.getId());
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

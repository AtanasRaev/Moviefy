package com.moviefy.utils;

import com.moviefy.database.model.dto.pageDto.SearchResultDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageWithGenreDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesPageWithGenreDTO;
import com.moviefy.service.MovieService;
import com.moviefy.service.TvSeriesService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.Normalizer;
import java.util.*;

public class SearchMediaUtil {

    private static final int MIN_QUERY_LENGTH = 2;

    public static ResponseEntity<Map<String, Object>> validateSearchQuery(String query) {
        if (query == null) {
            return ErrorResponseUtil.buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request", "The search query must not be empty!");
        }
        String normalized = normalizeQuery(query);
        if (normalized.isBlank() || normalized.length() < MIN_QUERY_LENGTH) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Invalid request",
                    "The search query must be at least " + MIN_QUERY_LENGTH + " characters long."
            );
        }
        return null;
    }

    public static ResponseEntity<Map<String, Object>> searchMedia(
            String query,
            int size,
            int page,
            MovieService movieService,
            TvSeriesService tvSeriesService) {

        ResponseEntity<Map<String, Object>> validationResponse = validateSearchQuery(query);
        if (validationResponse != null) return validationResponse;

        String normalized = normalizeQuery(query);
        int fetch = Math.max(size * page, size);
        Pageable fetchPageable = PageRequest.of(0, fetch);

        Page<MoviePageWithGenreDTO> movies = movieService.searchMovies(normalized, fetchPageable);
        Page<TvSeriesPageWithGenreDTO> series = tvSeriesService.searchTvSeries(normalized, fetchPageable);

        List<SearchResultDTO> combined = new ArrayList<>(
                movies.getNumberOfElements() + series.getNumberOfElements());
        combined.addAll(convertMoviesToSearchResults(movies.getContent()));
        combined.addAll(convertTvSeriesToSearchResults(series.getContent()));

        sortSearchResults(combined, normalized.toLowerCase(Locale.ROOT));

        int start = Math.min((page - 1) * size, combined.size());
        int end   = Math.min(start + size, combined.size());
        List<SearchResultDTO> paginatedResults = combined.subList(start, end);

        long totalItems = movies.getTotalElements() + series.getTotalElements();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        return ResponseEntity.ok(Map.of(
                "query", normalized,
                "items_on_page", paginatedResults.size(),
                "total_items", totalItems,
                "total_pages", totalPages,
                "current_page", page,
                "results", paginatedResults
        ));
    }

    private static String normalizeQuery(String q) {
        String s = q.trim().replaceAll("\\s+", " ");
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return s;
    }

    private static List<SearchResultDTO> convertMoviesToSearchResults(List<MoviePageWithGenreDTO> movies) {
        return movies.stream()
                .map(movie -> {
                    SearchResultDTO result = new SearchResultDTO();
                    result.setId(movie.getId());
                    result.setType("movie");
                    result.setTitle(movie.getTitle());
                    result.setPosterPath(movie.getPosterPath());
                    result.setVoteAverage(movie.getVoteAverage());
                    result.setYear(movie.getYear());
                    result.setGenre(movie.getGenre());
                    result.setTrailer(movie.getTrailer());
                    result.setRuntime(movie.getRuntime());
                    return result;
                })
                .toList();
    }

    private static List<SearchResultDTO> convertTvSeriesToSearchResults(List<TvSeriesPageWithGenreDTO> tvSeries) {
        return tvSeries.stream()
                .map(series -> {
                    SearchResultDTO result = new SearchResultDTO();
                    result.setId(series.getId());
                    result.setType("series");
                    result.setTitle(series.getName());
                    result.setPosterPath(series.getPosterPath());
                    result.setVoteAverage(series.getVoteAverage());
                    result.setYear(series.getYear());
                    result.setGenre(series.getGenre());
                    result.setTrailer(series.getTrailer());
                    result.setSeasonsCount(series.getSeasonsCount());
                    result.setEpisodesCount(series.getEpisodesCount());
                    return result;
                })
                .toList();
    }

    private static void sortSearchResults(List<SearchResultDTO> results, String queryLower) {
        results.sort((a, b) -> {
            String titleA = Optional.ofNullable(a.getTitle()).orElse("").toLowerCase(Locale.ROOT);
            String titleB = Optional.ofNullable(b.getTitle()).orElse("").toLowerCase(Locale.ROOT);

            int indexA = titleA.indexOf(queryLower);
            int indexB = titleB.indexOf(queryLower);

            if (indexA >= 0 && indexB >= 0) {
                if (indexA != indexB) return Integer.compare(indexA, indexB);
                if (titleA.length() != titleB.length()) return Integer.compare(titleA.length(), titleB.length());
                return titleA.compareTo(titleB);
            } else if (indexA >= 0) {
                return -1;
            } else if (indexB >= 0) {
                return 1;
            }
            return titleA.compareTo(titleB);
        });
    }
}
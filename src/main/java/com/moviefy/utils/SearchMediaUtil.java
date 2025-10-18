package com.moviefy.utils;

import com.moviefy.database.model.dto.pageDto.SearchResultDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageWithGenreDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesPageWithGenreDTO;
import com.moviefy.service.MovieService;
import com.moviefy.service.TvSeriesService;
import com.moviefy.service.impl.TvSeriesServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SearchMediaUtil {

    public static ResponseEntity<Map<String, Object>> validateSearchQuery(String query) {
        if (query == null || query.isBlank()) {
            return buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Invalid request",
                    "The search query must not be empty!"
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
        if (validationResponse != null) {
            return validationResponse;
        }

        Pageable pageable = PageRequest.of(page - 1, size / 2);

        Page<MoviePageWithGenreDTO> movies = movieService.searchMovies(query, pageable);
        Page<TvSeriesPageWithGenreDTO> series = tvSeriesService.searchTvSeries(query, pageable);

        List<SearchResultDTO> movieResults = convertMoviesToSearchResults(movies.getContent());
        List<SearchResultDTO> seriesResults = convertTvSeriesToSearchResults(series.getContent());

        List<SearchResultDTO> combinedResults = new ArrayList<>();
        combinedResults.addAll(movieResults);
        combinedResults.addAll(seriesResults);

        sortSearchResults(combinedResults, query.toLowerCase());
        
        int start = Math.min((page - 1) * size, combinedResults.size());
        int end = Math.min(start + size, combinedResults.size());
        List<SearchResultDTO> paginatedResults = combinedResults.subList(start, end);

        long totalItems = movies.getTotalElements() + series.getTotalElements();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        return ResponseEntity.ok(Map.of(
                "items_on_page", paginatedResults.size(),
                "total_items", totalItems,
                "total_pages", totalPages,
                "current_page", page,
                "results", paginatedResults
        ));
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
            String titleA = a.getTitle().toLowerCase();
            String titleB = b.getTitle().toLowerCase();

            int indexA = titleA.indexOf(queryLower);
            int indexB = titleB.indexOf(queryLower);

            if (indexA >= 0 && indexB >= 0) {
                if (indexA != indexB) {
                    return Integer.compare(indexA, indexB);
                }
                return Integer.compare(titleA.length(), titleB.length());
            } else if (indexA >= 0) {
                return -1;
            } else if (indexB >= 0) {
                return 1;
            }
            return titleA.compareTo(titleB);
        });
    }

    private static ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("error", error);
        response.put("message", message);

        return ResponseEntity
                .status(status)
                .body(response);
    }
}
package com.moviefy.web;

import com.moviefy.database.model.dto.detailsDto.MediaDetailsDTO;
import com.moviefy.service.media.MediaService;
import com.moviefy.service.media.movie.MovieService;
import com.moviefy.service.media.tvSeries.TvSeriesService;
import com.moviefy.service.media.tvSeries.TvSeriesServiceImpl;
import com.moviefy.utils.ErrorResponseUtil;
import com.moviefy.utils.SearchMediaUtil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class MediaController {
    private final MovieService movieService;
    private final TvSeriesService tvSeriesService;
    private final MediaService mediaService;
    private final SearchMediaUtil searchMediaUtil;

    public MediaController(MovieService movieService,
                           TvSeriesServiceImpl tvSeriesService,
                           MediaService mediaService,
                           SearchMediaUtil searchMediaUtil) {
        this.movieService = movieService;
        this.tvSeriesService = tvSeriesService;
        this.mediaService = mediaService;
        this.searchMediaUtil = searchMediaUtil;
    }

    @GetMapping("/{mediaType}/latest")
    public ResponseEntity<Map<String, Object>> getLatestMedia(
            @PathVariable String mediaType,
            @RequestParam(required = false) List<String> genres,
            @RequestParam(defaultValue = "10") @Min(4) @Max(100) int size,
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        if (isMediaTypeInvalid(mediaType)) {
            return getInvalidRequest(mediaType);
        }

        Pageable pageable;

        switch (mediaType) {
            case "movies" -> pageable = PageRequest.of(page - 1, size, Sort.by("release_date").descending());
            case "series" -> pageable = PageRequest.of(page - 1, size, Sort.by("first_air_date").descending());
            default -> pageable = PageRequest.of(page - 1, size, Sort.by("releaseDate").descending());
        }

        Page<?> mediaPage = getLatestMediaPage(mediaType, pageable, genres);

        return getMapResponseEntity(mediaType, mediaPage);
    }


    @GetMapping("/{mediaType}/{apiId}")
    public ResponseEntity<Map<String, Object>> getMediaByApiId(
            @PathVariable String mediaType,
            @PathVariable Long apiId) {

        if (isMediaTypeInvalid(mediaType)) {
            return getInvalidRequest(mediaType);
        }

        MediaDetailsDTO media = getMediaByApiIdPage(mediaType, apiId);

        if (media == null) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "Resource not found",
                    String.format("Not found %s with apiId %d", mediaType, apiId)
            );
        }

        return ResponseEntity.ok(Map.of(mediaType, media));
    }

    @GetMapping("/{mediaType}/trending")
    public ResponseEntity<Map<String, Object>> getMostTrendingMedia(
            @PathVariable String mediaType,
            @RequestParam(required = false) List<String> genres,
            @RequestParam(defaultValue = "10") @Min(4) @Max(100) int size,
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        if (isMediaTypeInvalid(mediaType)) {
            return getInvalidRequest(mediaType);
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("popularity").descending());
        Page<?> mediaPage = getTrendingMediaPage(mediaType, pageable, genres);

        return getMapResponseEntity(mediaType, mediaPage);
    }

    @GetMapping("/{mediaType}/popular")
    public ResponseEntity<Map<String, Object>> getPopularMedia(
            @PathVariable String mediaType,
            @RequestParam(defaultValue = "10") @Min(4) @Max(100) int size,
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        if (isMediaTypeInvalid(mediaType)) {
            return getInvalidRequest(mediaType);
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("voteCount").descending());
        Page<?> mediaPage = getPopularMediaPage(mediaType, pageable);

        return getMapResponseEntity(mediaType, mediaPage);
    }

    @GetMapping("/{mediaType}/search")
    public ResponseEntity<Map<String, Object>> searchMedia(
            @PathVariable String mediaType,
            @RequestParam("query") String query) {
        if (isMediaTypeInvalid(mediaType)) {
            return getInvalidRequest(mediaType);
        }

        List<?> result;

        switch (mediaType) {
            case "movies" -> result = this.movieService.searchMovies(query);
            case "series" -> result = this.tvSeriesService.searchTvSeries(query);
            default -> result = this.searchMediaUtil.search(query);
        }

        return ResponseEntity.ok(Map.of(
                "items_on_page", result.size(),
                "results", result
        ));
    }

    @GetMapping("/{mediaType}/genres")
    public ResponseEntity<Map<String, Object>> getMediaByGenre(@PathVariable String mediaType,
                                                               @RequestParam("genres") List<String> genres,
                                                               @RequestParam(defaultValue = "10") @Min(4) @Max(100) int size,
                                                               @RequestParam(defaultValue = "1") @Min(1) int page) {
        if (isMediaTypeInvalid(mediaType)) {
            return getInvalidRequest(mediaType);
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("popularity").descending());
        Page<?> mediaPage = getMediaByGenres(mediaType, genres, pageable);

        return getMapResponseEntity(mediaType, mediaPage);
    }

    @NotNull
    private ResponseEntity<Map<String, Object>> getMapResponseEntity(@PathVariable String mediaType, Page<?> mediaPage) {
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

    private boolean isMediaTypeInvalid(String mediaType) {
        return !"all".equalsIgnoreCase(mediaType) && !"movies".equalsIgnoreCase(mediaType) && !"series".equalsIgnoreCase(mediaType);
    }

    private Page<?> getTrendingMediaPage(String mediaType, Pageable pageable, List<String> genres) {
        return "movies".equalsIgnoreCase(mediaType)
                ? this.movieService.getTrendingMovies(genres, pageable)
                : "series".equalsIgnoreCase(mediaType)
                ? this.tvSeriesService.getTrendingTvSeries(genres, pageable)
                : this.mediaService.getTrendingMedia(genres, pageable);
    }

    private Page<?> getPopularMediaPage(String mediaType, Pageable pageable) {
        return "movies".equalsIgnoreCase(mediaType)
                ? movieService.getPopularMovies(pageable)
                : tvSeriesService.getPopularTvSeries(pageable);
    }

    private Page<?> getLatestMediaPage(String mediaType, Pageable pageable, List<String> genres) {
        return "movies".equalsIgnoreCase(mediaType)
                ? this.movieService.getMoviesFromCurrentMonth(pageable, genres)
                : "series".equalsIgnoreCase(mediaType)
                ? this.tvSeriesService.getTvSeriesFromCurrentMonth(pageable, genres)
                : this.mediaService.getLatestMedia(genres, pageable);
    }

    private MediaDetailsDTO getMediaByApiIdPage(String mediaType, Long apiId) {
        return "movies".equalsIgnoreCase(mediaType)
                ? movieService.getMovieDetailsByApiId(apiId)
                : tvSeriesService.getTvSeriesDetailsByApiId(apiId);
    }

    private ResponseEntity<Map<String, Object>> getInvalidRequest(String input) {
        return ErrorResponseUtil.buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request",
                String.format("The media type '%s' is invalid. It must be 'series' or 'movies'.", input)
        );
    }

    private Page<?> getMediaByGenres(String mediaType, List<String> genres, Pageable pageable) {
        return "movies".equalsIgnoreCase(mediaType)
                ? this.movieService.getMoviesByGenres(genres, pageable)
                : "series".equalsIgnoreCase(mediaType)
                ? this.tvSeriesService.getTvSeriesByGenres(genres, pageable)
                : this.mediaService.getMediaByGenres(genres, pageable);
    }
}

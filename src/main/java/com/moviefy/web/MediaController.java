package com.moviefy.web;

import com.moviefy.database.model.dto.databaseDto.EpisodeDTO;
import com.moviefy.database.model.dto.databaseDto.SeasonDTO;
import com.moviefy.database.model.dto.detailsDto.MediaDetailsDTO;
import com.moviefy.database.model.dto.detailsDto.MovieDetailsHomeDTO;
import com.moviefy.database.model.dto.pageDto.SearchResultDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.CollectionPageDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MovieHomeDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageWithGenreDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesPageWithGenreDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesTrendingPageDTO;
import com.moviefy.database.model.entity.media.Media;
import com.moviefy.service.MovieService;
import com.moviefy.service.TvSeriesService;
import com.moviefy.service.impl.MovieGenreServiceImpl;
import com.moviefy.service.impl.SeriesGenreServiceImpl;
import com.moviefy.service.impl.TvSeriesServiceImpl;
import com.moviefy.utils.ErrorResponseUtil;
import com.moviefy.utils.SearchMediaUtil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class MediaController {
    private final MovieService movieService;
    private final TvSeriesService tvSeriesService;

    public MediaController(MovieService movieService,
                           TvSeriesServiceImpl tvSeriesService) {
        this.movieService = movieService;
        this.tvSeriesService = tvSeriesService;
    }

    @GetMapping("/{mediaType}/latest")
    public ResponseEntity<Map<String, Object>> getLatestMedia(
            @PathVariable String mediaType,
            @RequestParam(defaultValue = "10") @Min(4) @Max(100) int size,
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        if (isMediaTypeInvalid(mediaType)) {
            return getInvalidRequest(mediaType);
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<?> mediaPage = getLatestMediaPage(mediaType, pageable);

        return ResponseEntity.ok(Map.of(
                "items_on_page", mediaPage.getNumberOfElements(),
                "total_items", mediaPage.getTotalElements(),
                "total_pages", mediaPage.getTotalPages(),
                "current_page", mediaPage.getNumber() + 1,
                mediaType, mediaPage.getContent()
        ));
    }


    @GetMapping("/{mediaType}/{id}")
    public ResponseEntity<Map<String, Object>> getMediaById(
            @PathVariable String mediaType,
            @PathVariable Long id) {

        if (isMediaTypeInvalid(mediaType)) {
            return getInvalidRequest(mediaType);
        }

        MediaDetailsDTO media = getMediaByIdPage(mediaType, id);

        if (media == null) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "Resource not found",
                    String.format("Not found %s with id %d", mediaType, id)
            );
        }

        return ResponseEntity.ok(Map.of(mediaType, media));
    }

    @GetMapping("/{mediaType}/trending")
    public ResponseEntity<Map<String, Object>> getMostTrendingMedia(
            @PathVariable String mediaType,
            @RequestParam(defaultValue = "10") @Min(4) @Max(100) int size,
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        if (isMediaTypeInvalid(mediaType)) {
            return getInvalidRequest(mediaType);
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<?> mediaPage = getTrendingMediaPage(mediaType, pageable);

        return ResponseEntity.ok(Map.of(
                "items_on_page", mediaPage.getNumberOfElements(),
                "total_items", mediaPage.getTotalElements(),
                "total_pages", mediaPage.getTotalPages(),
                "current_page", mediaPage.getNumber() + 1,
                mediaType, mediaPage.getContent()
        ));
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

        Map<String, Object> response = Map.of(
                "items_on_page", mediaPage.getNumberOfElements(),
                "total_items", mediaPage.getTotalElements(),
                "total_pages", mediaPage.getTotalPages(),
                "current_page", mediaPage.getNumber() + 1,
                mediaType, mediaPage.getContent()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchMedia(
            @RequestParam("query") String query,
            @RequestParam(defaultValue = "10") @Min(4) @Max(100) int size,
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        return SearchMediaUtil.searchMedia(query, size, page, movieService, tvSeriesService);
    }

    private boolean isMediaTypeInvalid(String mediaType) {
        return !"movies".equalsIgnoreCase(mediaType) && !"series".equalsIgnoreCase(mediaType);
    }

    private Page<?> getTrendingMediaPage(String mediaType, Pageable pageable) {
        return "movies".equalsIgnoreCase(mediaType)
                ? movieService.getTrendingMovies(pageable)
                : tvSeriesService.getTrendingTvSeries(pageable);
    }

    private Page<?> getPopularMediaPage(String mediaType, Pageable pageable) {
        return "movies".equalsIgnoreCase(mediaType)
                ? movieService.getPopularMovies(pageable)
                : tvSeriesService.getPopularTvSeries(pageable);
    }

    private Page<?> getLatestMediaPage(String mediaType, Pageable pageable) {
        return "movies".equalsIgnoreCase(mediaType)
                ? movieService.getMoviesFromCurrentMonth(pageable)
                : tvSeriesService.getTvSeriesFromCurrentMonth(pageable);
    }

    private MediaDetailsDTO getMediaByIdPage(String mediaType, Long id) {
        return "movies".equalsIgnoreCase(mediaType)
                ? movieService.getMovieDetailsById(id)
                : tvSeriesService.getTvSeriesDetailsById(id);
    }

    private ResponseEntity<Map<String, Object>> getInvalidRequest(String input) {
        return ErrorResponseUtil.buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request",
                String.format("The media type '%s' is invalid. It must be 'series' or 'movies'.", input)
        );
    }
}

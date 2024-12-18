package com.moviefy.web;

import com.moviefy.database.model.dto.detailsDto.MediaDetailsDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MovieDetailsHomeDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MovieHomeDTO;
import com.moviefy.service.MovieService;
import com.moviefy.service.impl.MovieGenreServiceImpl;
import com.moviefy.service.impl.SeriesGenreServiceImpl;
import com.moviefy.service.impl.TvSeriesServiceImpl;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MediaController {
    private final MovieService movieService;
    private final TvSeriesServiceImpl tvSeriesService;
    private final MovieGenreServiceImpl movieGenreService;
    private final SeriesGenreServiceImpl seriesGenreService;

    public MediaController(MovieService movieService, TvSeriesServiceImpl tvSeriesService,
                           MovieGenreServiceImpl movieGenreService, SeriesGenreServiceImpl seriesGenreService) {
        this.movieService = movieService;
        this.tvSeriesService = tvSeriesService;
        this.movieGenreService = movieGenreService;
        this.seriesGenreService = seriesGenreService;
    }

    @GetMapping("/{mediaType}/latest")
    public ResponseEntity<Map<String, Object>> getLatestMedia(
            @PathVariable String mediaType,
            @RequestParam(defaultValue = "20") @Min(10) @Max(100) int size,
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        if (isMediaTypeInvalid(mediaType)) {
            return getInvalidRequest(mediaType);
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<?> contentPage = getLatestMediaPage(mediaType, pageable);

        return ResponseEntity.ok(Map.of(
                "items_on_page", contentPage.getNumberOfElements(),
                "total_items", contentPage.getTotalElements(),
                "total_pages", contentPage.getTotalPages(),
                "current_page", contentPage.getNumber() + 1,
                mediaType, contentPage.getContent()
        ));
    }


    @GetMapping("/{mediaType}/{id}")
    public ResponseEntity<Map<String, Object>> getMediaById(
            @PathVariable String mediaType,
            @PathVariable Long id) {

        if (isMediaTypeInvalid(mediaType)) {
            return getInvalidRequest(mediaType);
        }

        MediaDetailsDTO media = fetchMediaById(mediaType, id);

        if (media == null) {
            return buildErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "Resource not found",
                    String.format("Not found %s with id %d", mediaType, id)
            );
        }

        return ResponseEntity.ok(Map.of(mediaType, media));
    }

    @GetMapping("/{mediaType}/trending")
    public ResponseEntity<Map<String, Object>> getMostTrendingMedia(@PathVariable String mediaType) {

        if (isMediaTypeInvalid(mediaType)) {
            return getInvalidRequest(mediaType);
        }

        int totalItems = 10;

        List<?> mediaList = getTrendingMediaList(mediaType, totalItems);

        return ResponseEntity.ok(Map.of(mediaType, mediaList));
    }

    @GetMapping("/{mediaType}/popular")
    public ResponseEntity<Map<String, Object>> getPopularMedia(
            @PathVariable String mediaType,
            @RequestParam(defaultValue = "20") @Min(10) @Max(100) int size,
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        if (isMediaTypeInvalid(mediaType)) {
            return getInvalidRequest(mediaType);
        }


        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("voteCount").descending());
        Page<?> mediaPage = getPopularMediaPage(mediaType, pageable);

        Map<String, Object> response = Map.of(
                mediaType, mediaPage.getContent(),
                "current_page", mediaPage.getNumber() + 1,
                "total_items", mediaPage.getTotalElements(),
                "total_pages", mediaPage.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("movie/collection")
    public ResponseEntity<Map<String, Object>> getCollectionSearch(@RequestParam("name") String input) {
        if (input == null || input.isBlank()) {
            return buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Invalid request",
                    "The search must not be empty!"
            );
        }


        MovieDetailsHomeDTO firstMovie = this.movieService.findFirstMovieByCollectionName(input);

        if (firstMovie == null) {
            return buildErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "Resource not found",
                    String.format("Not found collection '%s'", input)
            );
        }

        Map<String, Object> map = new LinkedHashMap<>();

        List<MovieHomeDTO> list = this.movieService.findMoviesByCollectionName(input)
                .stream()
                .skip(1)
                .toList();


        map.put("first_movie", firstMovie);
        map.put("rest_movies", list);
        return ResponseEntity.ok(map);
    }

//    @GetMapping("/{mediaType}/genre/{genreType}")
//    public ResponseEntity<Map<String, Object>> getGenres(
//            @PathVariable String mediaType,
//            @PathVariable String genreType,
//            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
//            @RequestParam(defaultValue = "1") @Min(1) int page) {
//
//        if (isMediaTypeInvalid(mediaType)) {
//            return ResponseEntity.badRequest().body(Map.of("error", "Invalid type: " + mediaType));
//        }
//
//        Pageable pageable = PageRequest.of(page - 1, size);
//        Page<?> contentPage;
//
//        if (isMediaTypeInvalid(mediaType)) {
//            return ResponseEntity.badRequest().body(Map.of("error", String.format("Invalid type: %s or genre: %s", mediaType, genreType)));
//        }
//
//        if (movieGenreService.getGenreByName(genreType) != null) {
//            media = movieService.getMoviesByGenre(genreType);
//        } else if (seriesGenreService.getGenreByName(genreType) != null) {
//            media = tvSeriesService.getTvSeriesByGenre(genreType);
//        } else {
//            return ResponseEntity.badRequest().body(Map.of("error", String.format("Invalid type: %s or genre: %s", mediaType, genreType)));
//        }
//
//        return ResponseEntity.ok(media);
//    }

    private boolean isMediaTypeInvalid(String mediaType) {
        return !"movie".equalsIgnoreCase(mediaType) && !"tv".equalsIgnoreCase(mediaType);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("error", error);
        response.put("message", message);

        return ResponseEntity
                .status(status)
                .body(response);
    }

    private List<?> getTrendingMediaList(String mediaType, int totalItems) {
        return "movie".equalsIgnoreCase(mediaType)
                ? movieService.getTrendingMovies(totalItems)
                : tvSeriesService.getTrendingTvSeries(totalItems);
    }

    private Page<?> getPopularMediaPage(String mediaType, Pageable pageable) {
        return "movie".equalsIgnoreCase(mediaType)
                ? movieService.getPopularMovies(pageable)
                : tvSeriesService.getPopularTvSeries(pageable);
    }

    private Page<?> getLatestMediaPage(String mediaType, Pageable pageable) {
        return "movie".equalsIgnoreCase(mediaType)
                ? movieService.getMoviesFromCurrentMonth(pageable)
                : tvSeriesService.getTvSeriesFromCurrentMonth(pageable);
    }

    private MediaDetailsDTO fetchMediaById(String mediaType, Long id) {
        return "movie".equalsIgnoreCase(mediaType)
                ? movieService.getMovieDetailsById(id)
                : tvSeriesService.getTvSeriesDetailsById(id);
    }

    private ResponseEntity<Map<String, Object>> getInvalidRequest(String input) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request",
                String.format("The media type '%s' is invalid. It must be 'tv' or 'movie'.", input)
        );
    }
}

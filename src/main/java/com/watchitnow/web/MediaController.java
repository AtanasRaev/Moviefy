package com.watchitnow.web;

import com.watchitnow.database.model.dto.detailsDto.MediaDetailsDTO;
import com.watchitnow.service.MovieService;
import com.watchitnow.service.impl.MovieGenreServiceImpl;
import com.watchitnow.service.impl.SeriesGenreServiceImpl;
import com.watchitnow.service.impl.TvSeriesServiceImpl;
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
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        if (isMediaTypeInvalid(mediaType)) {
            return getInvalidRequest(mediaType);
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("releaseDate").descending());
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


    @GetMapping("/{mediaType}/popularity")
    public ResponseEntity<Map<String, Object>> getMostPopularMedia(
            @PathVariable String mediaType,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        if (isMediaTypeInvalid(mediaType)) {
            return getInvalidRequest(mediaType);
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("popularity").descending());
        Page<?> contentPage = getMostPopularMediaPage(mediaType, pageable);

        return null;
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

    private Page<?> getMostPopularMediaPage(String mediaType, Pageable pageable) {
        return "movie".equalsIgnoreCase(mediaType)
                ? movieService.getMostPopularMovies(pageable)
                : tvSeriesService.getMostPopularTvSeries(pageable);
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

package com.watchitnow.web;

import com.watchitnow.database.model.dto.detailsDto.MediaDetailsDTO;
import com.watchitnow.service.MovieService;
import com.watchitnow.service.impl.MovieGenreServiceImpl;
import com.watchitnow.service.impl.SeriesGenreServiceImpl;
import com.watchitnow.service.impl.TvSeriesServiceImpl;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.*;
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
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid type: " + mediaType));
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("releaseDate").descending());
        Page<?> contentPage;

        if ("movie".equalsIgnoreCase(mediaType)) {
            contentPage = movieService.getMoviesFromCurrentMonth(pageable);
        } else {
            contentPage = tvSeriesService.getTvSeriesFromCurrentMonth(pageable);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items_on_page", contentPage.getNumberOfElements());
        response.put("total_items", contentPage.getTotalElements());
        response.put("total_pages", contentPage.getTotalPages());
        response.put("current_page", contentPage.getNumber() + 1);
        response.put(mediaType, contentPage.getContent());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{mediaType}/{id}")
    public ResponseEntity<Object> getMediaById(
            @PathVariable String mediaType,
            @PathVariable Long id) {

        MediaDetailsDTO media;

        if (isMediaTypeInvalid(mediaType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid type: " + mediaType));
        }

        if ("movie".equalsIgnoreCase(mediaType)) {
            media = movieService.getMovieById(id);
        } else {
            media = tvSeriesService.getTvSeriesById(id);
        }

        if (media == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid id: " + id));
        }

        return ResponseEntity.ok(media);
    }

//    @GetMapping("/{mediaType}/genre/{genreType}")
//    public ResponseEntity<Map<String, Object>> getGenres(
//        @PathVariable String mediaType,
//        @PathVariable String genreType,
//        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
//        @RequestParam(defaultValue = "1") @Min(1) int page) {
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
}

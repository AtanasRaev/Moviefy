package com.moviefy.web;

import com.moviefy.database.model.dto.detailsDto.MovieDetailsHomeDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.CollectionPageDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MovieHomeDTO;
import com.moviefy.service.MovieService;
import com.moviefy.utils.ErrorResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/movies")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/collection")
    public ResponseEntity<Map<String, Object>> getCollectionMoviesSearch(@RequestParam("name") String input) {
        if (input == null || input.isBlank()) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Invalid request",
                    "The search must not be empty!"
            );
        }

        MovieDetailsHomeDTO firstMovie = this.movieService.getFirstMovieByCollectionName(input);

        if (firstMovie == null) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "Resource not found",
                    String.format("Not found collection '%s'", input)
            );
        }

        Map<String, Object> map = new LinkedHashMap<>();

        List<MovieHomeDTO> list = this.movieService.getMoviesByCollectionName(input)
                .stream()
                .skip(1)
                .toList();

        map.put("first_movie", firstMovie);
        map.put("rest_movies", list);
        return ResponseEntity.ok(map);
    }

    @GetMapping("/collections")
    public ResponseEntity<Map<String, Object>> getCollectionSearch(@RequestParam("names") List<String> input) {
        if (input == null || input.isEmpty()) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Invalid request",
                    "The search must not be empty!"
            );
        }

        List<CollectionPageDTO> collections = this.movieService.getCollectionsByName(input);

        if (collections.isEmpty()) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "Resource not found",
                    String.format("Not found collections '%s'", input)
            );
        }

        return ResponseEntity.ok(Map.of(
                "collections", collections)
        );
    }
}

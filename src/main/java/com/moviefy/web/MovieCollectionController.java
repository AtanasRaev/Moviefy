package com.moviefy.web;

import com.moviefy.database.model.dto.pageDto.movieDto.CollectionPageDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.CollectionPageProjection;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageDTO;
import com.moviefy.service.collection.CollectionService;
import com.moviefy.utils.ErrorResponseUtil;
import com.moviefy.utils.ResponseUtil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/movies")
public class MovieCollectionController {
    private final CollectionService collectionService;

    public MovieCollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
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

        Map<String, Object> map = this.collectionService.getMoviesHomeByCollection(input);

        if (map == null || map.isEmpty()) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "Resource not found",
                    String.format("Not found collection '%s'", input)
            );
        }

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

        List<CollectionPageDTO> collections = this.collectionService.getCollectionsByName(input);

        if (collections == null || collections.isEmpty()) {
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

    @GetMapping("/collection/{apiId}")
    public ResponseEntity<Map<String, Object>> getMovieDetails(@PathVariable long apiId) {
        Map<String, List<MoviePageDTO>> moviesByApiId = this.collectionService.getMoviesByApiId(apiId);

        if (moviesByApiId == null || moviesByApiId.isEmpty()) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "Resource not found",
                    String.format("Not found collection with apiId '%d'", apiId)
            );
        }

        return ResponseEntity.ok(Map.of(
                "collection_name", moviesByApiId.entrySet().iterator().next().getKey(),
                "movies", moviesByApiId.entrySet().iterator().next().getValue()
        ));
    }

    @GetMapping("/collections/popular")
    public ResponseEntity<Map<String, Object>> getPopularCollections(
            @RequestParam(defaultValue = "10") @Min(4) @Max(100) int size,
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<CollectionPageProjection> collectionPage = this.collectionService.getPopular(pageable);

        if (collectionPage == null || collectionPage.isEmpty()) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "Resource not found",
                    "There are no collections"
            );
        }

        return ResponseUtil.getMapResponseEntity("results",collectionPage);
    }

    @GetMapping("/collections/search")
    public ResponseEntity<Map<String, Object>> searchCollections(
            @RequestParam("query") String query,
            @RequestParam(defaultValue = "10") @Min(4) @Max(100) int size,
            @RequestParam(defaultValue = "1") @Min(1) int page) {


        Pageable pageable = PageRequest.of(page - 1, size);
        Page<CollectionPageProjection> collectionPage = this.collectionService.searchCollections(query, pageable);

        return ResponseUtil.getMapResponseEntity("results", collectionPage);
    }
}

package com.watchitnow.web;

import com.watchitnow.databse.model.dto.MoviePageDTO;
import com.watchitnow.service.MovieService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/discover/movies")
    public ResponseEntity<?> getMovies(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "30") int count) {
        int size = 20;

        Pageable pageable = PageRequest.of(page - 1, size);
        Set<MoviePageDTO> allMovies = movieService.getMoviesFromCurrentMonth(count);

        int total = allMovies.size();
        int start = Math.min(pageable.getPageNumber() * pageable.getPageSize(), total);
        int end = Math.min(start + pageable.getPageSize(), total);

        List<MoviePageDTO> moviesForCurrentPage = allMovies.stream().toList().subList(start, end);

        Page<MoviePageDTO> moviesPage = new PageImpl<>(moviesForCurrentPage, pageable, total);

        Map<String, Object> response = new HashMap<>();
        response.put("movies", moviesPage.getContent());
        response.put("movies_on_page", moviesPage.getSize());
        response.put("total_items", moviesPage.getTotalElements());
        response.put("total_pages", moviesPage.getTotalPages());
        response.put("current_page", moviesPage.getNumber() + 1);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

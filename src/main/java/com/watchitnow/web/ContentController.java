package com.watchitnow.web;

import com.watchitnow.service.MovieService;
import com.watchitnow.service.impl.TvSeriesServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class ContentController {
    private final MovieService movieService;
    private final TvSeriesServiceImpl tvSeriesService;

    public ContentController(MovieService movieService,
                             TvSeriesServiceImpl tvSeriesService) {
        this.movieService = movieService;
        this.tvSeriesService = tvSeriesService;
    }

    @GetMapping("/{type}/latest")
    public ResponseEntity<Map<String, Object>> getLatestContent(
            @PathVariable String type,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(0, size);
        Page<?> contentPage;

        if ("movies".equalsIgnoreCase(type)) {
            contentPage = new PageImpl<>(new ArrayList<>(movieService.getMoviesFromCurrentMonth(size)), pageable, size);
        } else if ("tv-series".equalsIgnoreCase(type)) {
            contentPage = new PageImpl<>(new ArrayList<>(tvSeriesService.getTvSeriesFromCurrentMonth(size)), pageable, size);
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid type: " + type));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items_on_page", contentPage.getNumberOfElements());
        response.put("total_items", contentPage.getTotalElements());
        response.put("total_pages", contentPage.getTotalPages());
        response.put("current_page", contentPage.getNumber() + 1);
        response.put(type, contentPage.getContent());

        return ResponseEntity.ok(response);
    }
}

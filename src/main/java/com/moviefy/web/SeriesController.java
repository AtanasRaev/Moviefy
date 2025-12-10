package com.moviefy.web;

import com.moviefy.database.model.dto.databaseDto.EpisodeDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesTrendingPageDTO;
import com.moviefy.service.media.tvSeries.TvSeriesService;
import com.moviefy.service.media.tvSeries.seasons.SeasonsService;
import com.moviefy.utils.ErrorResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/series")
public class SeriesController {
    private final TvSeriesService tvSeriesService;
    private final SeasonsService seasonsService;

    public SeriesController(TvSeriesService tvSeriesService,
                            SeasonsService seasonsService) {
        this.tvSeriesService = tvSeriesService;
        this.seasonsService = seasonsService;
    }

    @GetMapping("/collection")
    public ResponseEntity<Map<String, Object>> getSeries(@RequestParam("names") List<String> input) {
        if (input == null || input.isEmpty()) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Invalid request",
                    "The search must not be empty!"
            );
        }

        List<TvSeriesTrendingPageDTO> series = this.tvSeriesService.getHomeSeriesDTO(input);

        if (series.isEmpty()) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "Resource not found",
                    String.format("Not found series '%s'", input)
            );
        }

        return ResponseEntity.ok(Map.of("series", series));
    }

    @GetMapping("/season/{id}")
    public ResponseEntity<Map<String, Object>> getEpisodesFromSeason(@PathVariable long id) {
        Integer seasonNumber = this.seasonsService.getSeasonNumberById(id);

        if (seasonNumber == null) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "Resource not found",
                    String.format("Not found season with id '%d'", id)
            );
        }

        List<EpisodeDTO> episodes = this.seasonsService.getEpisodesFromSeason(id);

        if (episodes.isEmpty()) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "Resource not found",
                    String.format("Not found any episodes from season with id '%d'", id)
            );
        }

        return ResponseEntity.ok(Map.of(seasonNumber.toString(), episodes));
    }
}

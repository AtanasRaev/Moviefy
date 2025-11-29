package com.moviefy.web;

import com.moviefy.service.media.MediaService;
import com.moviefy.service.media.movie.MovieService;
import com.moviefy.service.media.tvSeries.TvSeriesService;
import com.moviefy.utils.ErrorResponseUtil;
import com.moviefy.utils.MediaRetrievalUtil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/crew")
public class CrewController {
    private final MovieService movieService;
    private final TvSeriesService tvSeriesService;
    private final MediaService mediaService;

    public CrewController(MovieService movieService,
                          TvSeriesService tvSeriesService,
                          MediaService mediaService) {
        this.movieService = movieService;
        this.tvSeriesService = tvSeriesService;
        this.mediaService = mediaService;
    }

    @GetMapping("/{id}/media")
    public ResponseEntity<?> getMediaByCrew(
            @PathVariable("id") long id,
            @RequestParam("media_type") String mediaType,
            @RequestParam(defaultValue = "10") @Min(4) @Max(100) int size,
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        if (MediaRetrievalUtil.isMediaTypeInvalid(mediaType)) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Invalid request",
                    String.format("The media type '%s' is invalid. It must be 'series' or 'movies'.", mediaType)
            );
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<?> crewPage = getMediaPageByCrewId(mediaType, id, pageable);

        return ResponseEntity.ok(Map.of(
                "items_on_page", crewPage.getNumberOfElements(),
                "total_items", crewPage.getTotalElements(),
                "total_pages", crewPage.getTotalPages(),
                "current_page", crewPage.getNumber() + 1,
                mediaType, crewPage.getContent()
        ));
    }

    private Page<?> getMediaPageByCrewId(String mediaType, long id, Pageable pageable) {
        return "movies".equalsIgnoreCase(mediaType)
                ? this.movieService.getMoviesByCrewId(id, pageable)
                : "series".equalsIgnoreCase(mediaType)
                ? this.tvSeriesService.getTvSeriesByCrewId(id, pageable)
                : this.mediaService.getMediaByCastCrewId(id, pageable);
    }
}

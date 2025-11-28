package com.moviefy.web;

import com.moviefy.service.credit.cast.CastService;
import com.moviefy.service.media.MediaService;
import com.moviefy.service.media.movie.MovieService;
import com.moviefy.service.media.tvSeries.TvSeriesService;
import com.moviefy.utils.ErrorResponseUtil;
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
@RequestMapping("/cast")
public class CastController {
    private final CastService castService;
    private final MovieService movieService;
    private final TvSeriesService tvSeriesService;
    private final MediaService mediaService;

    public CastController(CastService castService,
                          MovieService movieService,
                          TvSeriesService tvSeriesService,
                          MediaService mediaService) {
        this.castService = castService;
        this.movieService = movieService;
        this.tvSeriesService = tvSeriesService;
        this.mediaService = mediaService;
    }

    @GetMapping("/{id}/media")
    public ResponseEntity<?> getMediaByCast(
            @PathVariable("id") long id,
            @RequestParam("media_type") String mediaType,
            @RequestParam(defaultValue = "10") @Min(4) @Max(100) int size,
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        if (isMediaTypeInvalid(mediaType)) {
            return ErrorResponseUtil.buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Invalid request",
                    String.format("The media type '%s' is invalid. It must be 'series' or 'movies'.", mediaType)
            );
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<?> castPage = getMediaPageByCastId(mediaType, id, pageable);

        return ResponseEntity.ok(Map.of(
                "items_on_page", castPage.getNumberOfElements(),
                "total_items", castPage.getTotalElements(),
                "total_pages", castPage.getTotalPages(),
                "current_page", castPage.getNumber() + 1,
                mediaType, castPage.getContent()
        ));
    }

    private boolean isMediaTypeInvalid(String mediaType) {
        return !"all".equalsIgnoreCase(mediaType) && !"movies".equalsIgnoreCase(mediaType) && !"series".equalsIgnoreCase(mediaType);
    }

    private Page<?> getMediaPageByCastId(String mediaType, long id, Pageable pageable) {
        return "movies".equalsIgnoreCase(mediaType)
                ? this.movieService.getMoviesByCastId(id, pageable)
                : "series".equalsIgnoreCase(mediaType)
                ? this.tvSeriesService.getTvSeriesByCastId(id, pageable)
                : this.mediaService.getMediaByCastId(id, pageable);
    }
}

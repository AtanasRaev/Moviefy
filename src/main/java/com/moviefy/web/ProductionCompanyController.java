package com.moviefy.web;

import com.moviefy.service.media.MediaService;
import com.moviefy.service.media.movie.MovieService;
import com.moviefy.service.media.tvSeries.TvSeriesService;
import com.moviefy.utils.ErrorResponseUtil;
import com.moviefy.utils.MediaRetrievalUtil;
import com.moviefy.utils.ResponseUtil;
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
@RequestMapping("/prod")
public class ProductionCompanyController {
    private final MovieService movieService;
    private final TvSeriesService tvSeriesService;
    private final MediaService mediaService;

    public ProductionCompanyController( MovieService movieService,
                                        TvSeriesService tvSeriesService,
                                        MediaService mediaService) {
        this.movieService = movieService;
        this.tvSeriesService = tvSeriesService;
        this.mediaService = mediaService;
    }

    @GetMapping("/{id}/media")
    public ResponseEntity<Map<String, Object>> getMediaByProductionCompany(
            @PathVariable long id,
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
        Page<?> mediaPage = getMediaPageByProductionCompany(mediaType, id, pageable);

        return ResponseUtil.getMapResponseEntity(mediaType, mediaPage);
    }

    private Page<?> getMediaPageByProductionCompany(String mediaType, long id, Pageable pageable) {
        return "movies".equalsIgnoreCase(mediaType)
                ? this.movieService.getMoviesByProductionCompanyId(id, pageable)
                : "series".equalsIgnoreCase(mediaType)
                ? this.tvSeriesService.getTvSeriesByProductionCompanyId(id, pageable)
                : this.mediaService.getMediaByProductionCompanyId(id, pageable);
    }
}

package com.moviefy.service.media;

import com.moviefy.database.model.dto.pageDto.MediaProjection;
import com.moviefy.database.repository.media.MediaRepository;
import com.moviefy.service.genre.movieGenre.MovieGenreService;
import com.moviefy.service.genre.seriesGenre.SeriesGenreService;
import com.moviefy.service.media.tvSeries.TvSeriesService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final TvSeriesService tvSeriesService;
    private final SeriesGenreService seriesGenreService;
    private final MovieGenreService movieGenreService;

    public MediaServiceImpl(MediaRepository mediaRepository,
                            TvSeriesService tvSeriesService,
                            SeriesGenreService seriesGenreService,
                            MovieGenreService movieGenreService) {
        this.mediaRepository = mediaRepository;
        this.tvSeriesService = tvSeriesService;
        this.seriesGenreService = seriesGenreService;
        this.movieGenreService = movieGenreService;
    }

    @Override
    @Cacheable(
            cacheNames = "mediaByGenres",
            key = "#genres + ';p=' + #pageable.pageNumber + ';s=' + #pageable.pageSize + ';sort=' + T(java.util.Objects).toString(#pageable.sort)",
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<MediaProjection> getMediaByGenres(List<String> genres, Pageable pageable) {
        List<String> lowerCaseSeriesGenres = this.tvSeriesService.getLowerCaseGenres(genres);
        List<String> lowerCaseMoviesGenres = genres.stream()
                .map(String::toLowerCase)
                .toList();

        return this.mediaRepository.findMediaByGenres(lowerCaseMoviesGenres, lowerCaseSeriesGenres, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = "latestMedia",
            key = "T(java.util.Objects).toString(#genres) + " +
                    "';p=' + #pageable.pageNumber + " +
                    "';s=' + #pageable.pageSize + " +
                    "';sort=' + T(java.util.Objects).toString(#pageable.sort)",
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<MediaProjection> getLatestMedia(List<String> genres, Pageable pageable) {

        List<String> seriesGenres;
        List<String> movieGenres;

        if (genres == null || genres.isEmpty()) {
            seriesGenres = this.seriesGenreService.getAllGenresNames();
            movieGenres  = this.movieGenreService.getAllGenresNames();
        } else {
            seriesGenres = genres;
            movieGenres  = genres;
        }

        List<String> lowerCaseSeriesGenres = this.tvSeriesService.getLowerCaseGenres(seriesGenres);
        List<String> lowerCaseMoviesGenres = movieGenres.stream()
                .map(String::toLowerCase)
                .toList();

        Page<MediaProjection> page =
                mediaRepository.findMediaByGenres(lowerCaseMoviesGenres, lowerCaseSeriesGenres, pageable);

        LocalDate threshold = LocalDate.now().minusDays(7);

        List<MediaProjection> filtered = page.getContent().stream()
                .filter(item -> !item.getReleaseDate().isAfter(threshold))
                .toList();

        return new PageImpl<>(filtered, pageable, filtered.size());
    }
}

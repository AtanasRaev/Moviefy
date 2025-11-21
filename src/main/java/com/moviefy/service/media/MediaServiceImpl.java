package com.moviefy.service.media;

import com.moviefy.database.model.dto.pageDto.MediaProjection;
import com.moviefy.database.model.dto.pageDto.MediaWithGenreProjection;
import com.moviefy.database.repository.media.MediaRepository;
import com.moviefy.utils.GenreNormalizationUtil;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MediaServiceImpl implements MediaService {
    private final MediaRepository mediaRepository;
    private final GenreNormalizationUtil genreNormalizationUtil;

    public MediaServiceImpl(MediaRepository mediaRepository,
                            GenreNormalizationUtil genreNormalizationUtil) {
        this.mediaRepository = mediaRepository;
        this.genreNormalizationUtil = genreNormalizationUtil;
    }

    @Override
    @Cacheable(
            cacheNames = "mediaByGenres",
            key = "#genres + ';p=' + #pageable.pageNumber + ';s=' + #pageable.pageSize + ';sort=' + T(java.util.Objects).toString(#pageable.sort)",
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<MediaProjection> getMediaByGenres(List<String> genres, Pageable pageable) {
        List<String> lowerCaseSeriesGenres = this.genreNormalizationUtil.getSeriesLowerCaseGenres(genres);
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
        List<String> lowerCaseMoviesGenres = this.genreNormalizationUtil.processMovieGenres(genres);
        List<String> lowerCaseSeriesGenres = this.genreNormalizationUtil.processSeriesGenres(genres);

        return this.mediaRepository.findLatestMedia(getStartOfCurrentMonth(), lowerCaseMoviesGenres, lowerCaseSeriesGenres, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = "trendingMedia",
            key = "T(java.util.Objects).toString(#genres) + " +
                    "';p=' + #pageable.pageNumber + " +
                    "';s=' + #pageable.pageSize + " +
                    "';sort=' + T(java.util.Objects).toString(#pageable.sort)",
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<MediaWithGenreProjection> getTrendingMedia(List<String> genres, Pageable pageable) {
        List<String> lowerCaseMoviesGenres = this.genreNormalizationUtil.processMovieGenres(genres);
        List<String> lowerCaseSeriesGenres = this.genreNormalizationUtil.processSeriesGenres(genres);

        return this.mediaRepository.findTrendingMedia(lowerCaseMoviesGenres, lowerCaseSeriesGenres, pageable);
    }

    private LocalDate getStartOfCurrentMonth() {
        return LocalDate.now().minusDays(7);
    }
}

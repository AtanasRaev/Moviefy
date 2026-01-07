package com.moviefy.service.media;

import com.moviefy.config.cache.CacheKeys;
import com.moviefy.database.model.dto.apiDto.reviewDto.ReviewResponseApiDTO;
import com.moviefy.database.model.dto.pageDto.mediaDto.MediaProjection;
import com.moviefy.database.model.dto.pageDto.mediaDto.MediaWithGenreProjection;
import com.moviefy.database.model.dto.pageDto.ReviewPageDTO;
import com.moviefy.database.repository.media.MediaRepository;
import com.moviefy.service.api.TmdbCommonEndpointService;
import com.moviefy.utils.GenreNormalizationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class MediaServiceImpl implements MediaService {
    private final MediaRepository mediaRepository;
    private final TmdbCommonEndpointService tmdbCommonEndpointService;
    private final GenreNormalizationUtil genreNormalizationUtil;
    private final ModelMapper modelMapper;

    public MediaServiceImpl(MediaRepository mediaRepository,
                            TmdbCommonEndpointService tmdbCommonEndpointService,
                            GenreNormalizationUtil genreNormalizationUtil,
                            ModelMapper modelMapper) {
        this.mediaRepository = mediaRepository;
        this.tmdbCommonEndpointService = tmdbCommonEndpointService;
        this.genreNormalizationUtil = genreNormalizationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.MEDIA_BY_GENRES,
            key = "#genres + ';p=' + #pageable.pageNumber + ';s=' + #pageable.pageSize + ';sort=' + T(java.util.Objects).toString(#pageable.sort)",
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<MediaProjection> getMediaByGenres(List<String> genres, Pageable pageable) {
        List<String> lowerCaseSeriesGenres = this.genreNormalizationUtil.processSeriesGenres(genres);
        List<String> lowerCaseMoviesGenres = this.genreNormalizationUtil.processMovieGenres(genres);

        return this.mediaRepository.findMediaByGenres(lowerCaseMoviesGenres, lowerCaseSeriesGenres, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.LATEST_MEDIA,
            key = "T(java.util.Objects).toString(#genres) + " +
                    "';p=' + #pageable.pageNumber + " +
                    "';s=' + #pageable.pageSize + " +
                    "';sort=' + T(java.util.Objects).toString(#pageable.sort)",
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<MediaProjection> getLatestMedia(List<String> genres, Pageable pageable) {
        List<String> lowerCaseMoviesGenres = this.genreNormalizationUtil.processMovieGenres(genres);
        List<String> lowerCaseSeriesGenres = this.genreNormalizationUtil.processSeriesGenres(genres);

        return this.mediaRepository.findLatestMedia(getStartOfCurrentMonth(), lowerCaseMoviesGenres, lowerCaseSeriesGenres, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.TRENDING_MEDIA,
            key = "T(java.util.Objects).toString(#genres) + " +
                    "';p=' + #pageable.pageNumber + " +
                    "';s=' + #pageable.pageSize + " +
                    "';sort=' + T(java.util.Objects).toString(#pageable.sort)",
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<MediaWithGenreProjection> getTrendingMedia(List<String> genres, Pageable pageable) {
        List<String> lowerCaseMoviesGenres = this.genreNormalizationUtil.processMovieGenres(genres);
        List<String> lowerCaseSeriesGenres = this.genreNormalizationUtil.processSeriesGenres(genres);

        return this.mediaRepository.findAllByGenresMapped(lowerCaseMoviesGenres, lowerCaseSeriesGenres, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.POPULAR_MEDIA,
            key = "T(java.util.Objects).toString(#genres) + " +
                    "';p=' + #pageable.pageNumber + " +
                    "';s=' + #pageable.pageSize + " +
                    "';sort=' + T(java.util.Objects).toString(#pageable.sort)",
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<MediaWithGenreProjection> getPopularMedia(List<String> genres, Pageable pageable) {
        List<String> lowerCaseMoviesGenres = this.genreNormalizationUtil.processMovieGenres(genres);
        List<String> lowerCaseSeriesGenres = this.genreNormalizationUtil.processSeriesGenres(genres);

        return this.mediaRepository.findAllByGenresMapped(lowerCaseMoviesGenres, lowerCaseSeriesGenres, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.TOP_RATED_MEDIA,
            key = "T(java.util.Objects).toString(#genres) + " +
                    "';p=' + #pageable.pageNumber + " +
                    "';s=' + #pageable.pageSize + " +
                    "';sort=' + T(java.util.Objects).toString(#pageable.sort)",
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<MediaWithGenreProjection> getTopRatedMedia(List<String> genres, Pageable pageable) {
        List<String> lowerCaseMoviesGenres = this.genreNormalizationUtil.processMovieGenres(genres);
        List<String> lowerCaseSeriesGenres = this.genreNormalizationUtil.processSeriesGenres(genres);

        return this.mediaRepository.findTopRatedCombinedByGenres(lowerCaseMoviesGenres, lowerCaseSeriesGenres, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.MEDIA_BY_CAST,
            key = """
                    'cast=' + #id
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<MediaProjection> getMediaByCastId(long id, Pageable pageable) {
        return this.mediaRepository.findTopRatedMediaByCastId(id, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.MEDIA_BY_CREW,
            key = """
                    'crew=' + #id
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<MediaProjection> getMediaByCastCrewId(long id, Pageable pageable) {
        return this.mediaRepository.findTopRatedMediaByCrewId(id, pageable);
    }

    @Override
    public Map<String, Object> getReviewsByApiId(String mediaType, long apiId, int page) {
        ReviewResponseApiDTO reviewsResponseApi = this.tmdbCommonEndpointService.getReviewsResponseApi(mediaType,apiId, page);

        if (reviewsResponseApi == null || reviewsResponseApi.getResults() == null) {
            return Map.of();
        }


        List<ReviewPageDTO> reviews = reviewsResponseApi.getResults().stream()
                .map(r -> {
                    ReviewPageDTO map = this.modelMapper.map(r, ReviewPageDTO.class);
                    map.setAuthorPath(r.getAuthorDetails().getAvatarPath());
                    map.setRating(r.getAuthorDetails().getRating());

                    return map;
                })
                .toList();

        return Map.of(
                "items_on_page", reviews.size(),
                "total_items", reviewsResponseApi.getTotalResults(),
                "total_pages", reviewsResponseApi.getTotalPages(),
                "current_page", reviewsResponseApi.getPage(),
                "reviews", reviews
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MediaProjection> getMediaByProductionCompanyId(long id, Pageable pageable) {
        return this.mediaRepository.findTopRatedMediaByProductionCompanyId(id, pageable);
    }

    private LocalDate getStartOfCurrentMonth() {
        return LocalDate.now().minusDays(7);
    }
}

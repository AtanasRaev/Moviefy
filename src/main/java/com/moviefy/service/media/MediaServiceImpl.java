package com.moviefy.service.media;

import com.moviefy.config.ApiConfig;
import com.moviefy.database.model.dto.apiDto.ReviewResponseApiDTO;
import com.moviefy.database.model.dto.pageDto.MediaProjection;
import com.moviefy.database.model.dto.pageDto.MediaWithGenreProjection;
import com.moviefy.database.model.dto.pageDto.ReviewPageDTO;
import com.moviefy.database.repository.media.MediaRepository;
import com.moviefy.utils.GenreNormalizationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class MediaServiceImpl implements MediaService {
    private final MediaRepository mediaRepository;
    private final GenreNormalizationUtil genreNormalizationUtil;
    private final ApiConfig apiConfig;
    private final RestClient restClient;
    private final ModelMapper modelMapper;

    public MediaServiceImpl(MediaRepository mediaRepository,
                            GenreNormalizationUtil genreNormalizationUtil,
                            ApiConfig apiConfig,
                            RestClient restClient,
                            ModelMapper modelMapper) {
        this.mediaRepository = mediaRepository;
        this.genreNormalizationUtil = genreNormalizationUtil;
        this.apiConfig = apiConfig;
        this.restClient = restClient;
        this.modelMapper = modelMapper;
    }

    @Override
    @Cacheable(
            cacheNames = "mediaByGenres",
            key = "#genres + ';p=' + #pageable.pageNumber + ';s=' + #pageable.pageSize + ';sort=' + T(java.util.Objects).toString(#pageable.sort)",
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<MediaProjection> getMediaByGenres(List<String> genres, Pageable pageable) {
        List<String> lowerCaseSeriesGenres = this.genreNormalizationUtil.processSeriesGenres(genres);
        List<String> lowerCaseMoviesGenres = this.genreNormalizationUtil.processMovieGenres(genres);

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

        return this.mediaRepository.findAllByGenresMapped(lowerCaseMoviesGenres, lowerCaseSeriesGenres, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = "popularMedia",
            key = "T(java.util.Objects).toString(#genres) + " +
                    "';p=' + #pageable.pageNumber + " +
                    "';s=' + #pageable.pageSize + " +
                    "';sort=' + T(java.util.Objects).toString(#pageable.sort)",
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<MediaWithGenreProjection> getPopularMedia(List<String> genres, Pageable pageable) {
        List<String> lowerCaseMoviesGenres = this.genreNormalizationUtil.processMovieGenres(genres);
        List<String> lowerCaseSeriesGenres = this.genreNormalizationUtil.processSeriesGenres(genres);

        return this.mediaRepository.findAllByGenresMapped(lowerCaseMoviesGenres, lowerCaseSeriesGenres, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = "topRatedMedia",
            key = "T(java.util.Objects).toString(#genres) + " +
                    "';p=' + #pageable.pageNumber + " +
                    "';s=' + #pageable.pageSize + " +
                    "';sort=' + T(java.util.Objects).toString(#pageable.sort)",
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<MediaWithGenreProjection> getTopRatedMedia(List<String> genres, Pageable pageable) {
        List<String> lowerCaseMoviesGenres = this.genreNormalizationUtil.processMovieGenres(genres);
        List<String> lowerCaseSeriesGenres = this.genreNormalizationUtil.processSeriesGenres(genres);

        return this.mediaRepository.findTopRatedCombinedByGenres(lowerCaseMoviesGenres, lowerCaseSeriesGenres, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = "mediaByCast",
            key = """
                    'cast=' + #id
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<MediaProjection> getMediaByCastId(long id, Pageable pageable) {
        return this.mediaRepository.findTopRatedMediaByCastId(id, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = "mediaByCrew",
            key = """
                    'crew=' + #id
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<MediaProjection> getMediaByCastCrewId(long id, Pageable pageable) {
        return this.mediaRepository.findTopRatedMediaByCrewId(id, pageable);
    }

    @Override
    public Map<String, Object> getReviewsByApiId(String mediaType, long apiId, int page) {
        ReviewResponseApiDTO reviewsResponseApi = this.getReviewsResponseApi(mediaType,apiId, page);

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
    public Page<MediaProjection> getMediaByProductionCompanyId(long id, Pageable pageable) {
        return this.mediaRepository.findTopRatedMediaByProductionCompanyId(id, pageable);
    }

    private LocalDate getStartOfCurrentMonth() {
        return LocalDate.now().minusDays(7);
    }

    private ReviewResponseApiDTO getReviewsResponseApi(String mediaType, long apiId, int page) {
        String type = mediaType.toLowerCase();

        if (mediaType.equals("series")) {
            type = "tv";
        } else {
            type = "movie";
        }

        String url = String.format(this.apiConfig.getUrl()
                        + "/%s/%d/reviews?api_key=%s&page=%d",
                type, apiId, this.apiConfig.getKey(), page);

        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(ReviewResponseApiDTO.class);
        } catch (Exception e) {
            System.err.println("Error searching movies" + "- " + e.getMessage());
            return null;
        }

    }
}

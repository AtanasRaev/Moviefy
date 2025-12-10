package com.moviefy.service.media;

import com.moviefy.database.model.dto.pageDto.mediaDto.MediaProjection;
import com.moviefy.database.model.dto.pageDto.mediaDto.MediaWithGenreProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface MediaService {
    Page<MediaProjection> getMediaByGenres(List<String> genres, Pageable pageable);

    Page<MediaProjection> getLatestMedia(List<String> genres, Pageable pageable);

    Page<MediaWithGenreProjection> getTrendingMedia(List<String> genres, Pageable pageable);

    Page<MediaWithGenreProjection> getPopularMedia(List<String> genres, Pageable pageable);

    Page<MediaWithGenreProjection> getTopRatedMedia(List<String> genres, Pageable pageable);

    Page<MediaProjection> getMediaByCastId(long id, Pageable pageable);

    Page<MediaProjection> getMediaByCastCrewId(long id, Pageable pageable);

    Map<String, Object> getReviewsByApiId(String mediaType, long apiId, int page);

    Page<MediaProjection> getMediaByProductionCompanyId(long id, Pageable pageable);
}

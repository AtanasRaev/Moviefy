package com.moviefy.service.media;

import com.moviefy.database.model.dto.pageDto.MediaProjection;
import com.moviefy.database.model.dto.pageDto.MediaWithGenreProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MediaService {
    Page<MediaProjection> getMediaByGenres(List<String> genres, Pageable pageable);

    Page<MediaProjection> getLatestMedia(List<String> genres, Pageable pageable);

    Page<MediaWithGenreProjection> getTrendingMedia(List<String> genres, Pageable pageable);

    Page<MediaWithGenreProjection> getPopularMedia(List<String> genres, Pageable pageable);

    Page<MediaWithGenreProjection> getTopRatedMedia(List<String> genres, Pageable pageable);

    Page<MediaProjection> getMediaByCastId(long id, Pageable pageable);

    Page<MediaProjection> getMediaByCastCrewId(long id, Pageable pageable);
}

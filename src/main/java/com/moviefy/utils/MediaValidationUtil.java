package com.moviefy.utils;

import com.moviefy.database.model.dto.apiDto.MediaApiDTO;
import com.moviefy.database.model.dto.apiDto.MovieApiDTO;
import com.moviefy.database.model.dto.apiDto.TvSeriesApiDTO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

public class MediaValidationUtil {
    public static boolean isInvalid(MediaApiDTO dto) {
        String name = "";
        if (dto instanceof TvSeriesApiDTO tvSeriesDto) {
            name = tvSeriesDto.getName();
        } else if (dto instanceof MovieApiDTO mediaDto) {
            name = mediaDto.getTitle();
        }

        return dto.getPosterPath() == null || dto.getPosterPath().isBlank()
                || dto.getOverview() == null || dto.getOverview().isBlank()
                || name == null || name.isBlank()
                || dto.getBackdropPath() == null || dto.getBackdropPath().isBlank();
    }

    public static boolean isValidForUpdate(MediaApiDTO dto, LocalDate now, Set<Long> trendingIds) {
        LocalDate releaseDate = null;
        if (dto instanceof TvSeriesApiDTO tvSeriesDTO) {
            releaseDate = tvSeriesDTO.getFirstAirDate();
        } else if (dto instanceof MovieApiDTO movieDTO) {
            releaseDate =  movieDTO.getReleaseDate();
        }

        if (isInvalid(dto) || releaseDate == null) {
            return false;
        }
        long days = ChronoUnit.DAYS.between(releaseDate, now);
        if (days <= 7) {
            return dto.getPopularity() >= 5 || trendingIds.contains(dto.getId());
        }
        if (days <= 30) {
            return dto.getVoteCount() >= 5 || dto.getPopularity() >= 10;
        }
        return dto.getVoteCount() >= 20 || dto.getPopularity() >= 20;
    }
}

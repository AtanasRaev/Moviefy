package com.moviefy.utils.mappers;

import com.moviefy.database.model.dto.apiDto.mediaDto.MediaApiByIdResponseDTO;
import com.moviefy.database.model.entity.media.Media;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

public class MediaRefreshMapper {
    public static boolean mapCommonFields(Media media, MediaApiByIdResponseDTO dto, LocalDateTime refreshedAt) {

        boolean isUpdated = false;

        double newPopularity = BigDecimal.valueOf(dto.getPopularity())
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();

        double newVoteAverage = BigDecimal.valueOf(dto.getVoteAverage())
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();

        if (!Objects.equals(media.getPopularity(), newPopularity)) {
            media.setPopularity(newPopularity);
            isUpdated = true;
        }

        if (!Objects.equals(media.getVoteAverage(), newVoteAverage)) {
            media.setVoteAverage(newVoteAverage);
            isUpdated = true;
        }

        if (!Objects.equals(media.getVoteCount(), dto.getVoteCount())) {
            media.setVoteCount(dto.getVoteCount());
            isUpdated = true;
        }

        if (!Objects.equals(media.isAdult(), dto.isAdult())) {
            media.setAdult(dto.isAdult());
            isUpdated = true;
        }

        if (isUpdated) {
            media.setRefreshedAt(refreshedAt);
        }

        return isUpdated;
    }

}

package com.moviefy.utils.mappers;

import com.moviefy.database.model.dto.apiDto.mediaDto.tvSeriesDto.TvSeriesApiByIdResponseDTO;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;
import com.moviefy.service.media.tvSeries.seasons.SeasonsService;

import java.time.LocalDateTime;
import java.util.Objects;

public class TvSeriesRefreshMapper {
    public static boolean mapTvSeries(TvSeries tvSeries, TvSeriesApiByIdResponseDTO dto, LocalDateTime refreshedAt, SeasonsService seasonsService) {
        boolean isUpdated = MediaRefreshMapper.mapCommonFields(tvSeries, dto, refreshedAt);

        if (!Objects.equals(tvSeries.getNumberOfSeasons(), dto.getNumberOfSeasons()) || !Objects.equals(tvSeries.getNumberOfEpisodes(), dto.getNumberOfEpisodes())) {
            tvSeries.setNumberOfSeasons(dto.getNumberOfSeasons());
            tvSeries.setNumberOfEpisodes(dto.getNumberOfEpisodes());

            isUpdated = true;
        }

        boolean isUpdatedSeasonsOrEpisodes = seasonsService.updateSeasonsAndEpisodes(dto.getSeasons(), tvSeries);

        if (isUpdatedSeasonsOrEpisodes) {
            isUpdated = true;
        }

        if (!Objects.equals(tvSeries.getStatus(), dto.getStatus())) {
            tvSeries.setStatus(dto.getStatus());
            isUpdated = true;
        }

        if (!Objects.equals(tvSeries.getType(), dto.getType())) {
            tvSeries.setType(dto.getType());
            isUpdated = true;
        }

        return isUpdated;
    }
}

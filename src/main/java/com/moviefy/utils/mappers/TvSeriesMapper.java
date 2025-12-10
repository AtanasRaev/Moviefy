package com.moviefy.utils.mappers;

import com.moviefy.database.model.dto.apiDto.mediaDto.TrailerResponseApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.tvSeriesDto.TvSeriesApiByIdResponseDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.tvSeriesDto.TvSeriesApiDTO;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;
import com.moviefy.service.genre.seriesGenre.SeriesGenreService;
import org.springframework.stereotype.Component;

@Component
public class TvSeriesMapper extends MediaMapper {
    private final SeriesGenreService seriesGenreService;

    public TvSeriesMapper(SeriesGenreService seriesGenreService) {
        this.seriesGenreService = seriesGenreService;
    }

    public TvSeries mapToTvSeries(TvSeriesApiDTO dto, TvSeriesApiByIdResponseDTO responseById, TrailerResponseApiDTO responseTrailer) {
        TvSeries tvSeries = new TvSeries();

        super.mapCommonFields(tvSeries, dto, responseTrailer);
        tvSeries.setName(dto.getName());
        tvSeries.setOriginalName(!dto.getOriginalName().equals(dto.getName()) && !dto.getOriginalName().isBlank() ? dto.getOriginalName() : null);
        tvSeries.setFirstAirDate(dto.getFirstAirDate());
        tvSeries.setRankingYear(dto.getFirstAirDate().getYear());
        tvSeries.setGenres(this.seriesGenreService.getAllGenresByApiIds(dto.getGenres()));
        tvSeries.setAdult(dto.isAdult());
        tvSeries.setType(responseById.getType());
        tvSeries.setNumberOfSeasons(responseById.getNumberOfSeasons());
        tvSeries.setNumberOfEpisodes(responseById.getNumberOfEpisodes());
        if (responseById.getExternalIds() != null) {
            tvSeries.setImdbId(responseById.getExternalIds().getImdbId() == null || responseById.getExternalIds().getImdbId().isBlank() ? null : responseById.getExternalIds().getImdbId());
        }

        if (responseById.getStatus() != null && !responseById.getStatus().isBlank()) {
            tvSeries.setStatus(responseById.getStatus());
        }

        return tvSeries;
    }
}


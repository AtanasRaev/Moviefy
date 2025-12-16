package com.moviefy.utils.mappers;

import com.moviefy.database.model.dto.apiDto.mediaDto.TrailerResponseApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.tvSeriesDto.TvSeriesApiByIdResponseDTO;
import com.moviefy.database.model.dto.databaseDto.GenreDTO;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;
import com.moviefy.service.genre.seriesGenre.SeriesGenreService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TvSeriesMapper extends MediaMapper {
    private final SeriesGenreService seriesGenreService;

    public TvSeriesMapper(SeriesGenreService seriesGenreService) {
        this.seriesGenreService = seriesGenreService;
    }

    public TvSeries mapToTvSeries(TvSeriesApiByIdResponseDTO dto, TrailerResponseApiDTO responseTrailer) {
        TvSeries tvSeries = new TvSeries();

        super.mapCommonFields(tvSeries, dto, responseTrailer);
        tvSeries.setName(dto.getName());
        tvSeries.setOriginalName(!dto.getOriginalName().equals(dto.getName()) && !dto.getOriginalName().isBlank() ? dto.getOriginalName() : null);
        tvSeries.setFirstAirDate(dto.getFirstAirDate());
        tvSeries.setRankingYear(dto.getFirstAirDate().getYear());
        if (dto.getGenres() != null && !dto.getGenres().isEmpty()) {
            Set<Long> genreApiIds = dto.getGenres().stream()
                    .map(GenreDTO::getId)
                    .collect(Collectors.toSet());
            tvSeries.setGenres(this.seriesGenreService.getAllGenresByApiIds(genreApiIds));
        }
        tvSeries.setAdult(dto.isAdult());
        tvSeries.setType(dto.getType());
        tvSeries.setNumberOfSeasons(dto.getNumberOfSeasons());
        tvSeries.setNumberOfEpisodes(dto.getNumberOfEpisodes());
        if (dto.getExternalIds() != null) {
            tvSeries.setImdbId(dto.getExternalIds().getImdbId() == null || dto.getExternalIds().getImdbId().isBlank() ? null : dto.getExternalIds().getImdbId());
        }

        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            tvSeries.setStatus(dto.getStatus());
        }

        tvSeries.setInsertedAt(dto.getFirstAirDate().isBefore(LocalDate.now()) || dto.getFirstAirDate().isEqual(LocalDate.now())
                ? LocalDateTime.now()
                : dto.getFirstAirDate().atStartOfDay());

        return tvSeries;
    }
}


package com.watchitnow.utils;

import com.watchitnow.database.model.dto.apiDto.TrailerResponseApiDTO;
import com.watchitnow.database.model.dto.apiDto.TvSeriesApiByIdResponseDTO;
import com.watchitnow.database.model.dto.apiDto.TvSeriesApiDTO;
import com.watchitnow.database.model.entity.media.StatusTvSeries;
import com.watchitnow.database.model.entity.media.TvSeries;
import com.watchitnow.database.repository.StatusTvSeriesRepository;
import com.watchitnow.service.SeriesGenreService;
import org.springframework.stereotype.Component;

@Component
public class TvSeriesMapper extends MediaMapper {

    private final SeriesGenreService seriesGenreService;
    private final StatusTvSeriesRepository statusTvSeriesRepository;

    public TvSeriesMapper(TrailerMappingUtil trailerMappingUtil,
                          SeriesGenreService seriesGenreService,
                          StatusTvSeriesRepository statusTvSeriesRepository) {
        super(trailerMappingUtil);
        this.seriesGenreService = seriesGenreService;
        this.statusTvSeriesRepository = statusTvSeriesRepository;
    }

    public TvSeries mapToTvSeries(TvSeriesApiDTO dto, TvSeriesApiByIdResponseDTO responseById, TrailerResponseApiDTO responseTrailer) {
        TvSeries tvSeries = new TvSeries();

        super.mapCommonFields(tvSeries, dto, responseTrailer);
        tvSeries.setName(dto.getName());
        tvSeries.setOriginalName(!dto.getOriginalName().equals(dto.getName()) && !dto.getOriginalName().isBlank() ? dto.getOriginalName() : null);
        tvSeries.setFirstAirDate(dto.getFirstAirDate());
        tvSeries.setEpisodeRunTime(getEpisodeRunTime(responseById));
        tvSeries.setGenres(this.seriesGenreService.getAllGenresByApiIds(dto.getGenres()));

        if (responseById.getStatus() != null && !responseById.getStatus().isBlank()) {
            StatusTvSeries status = findByName(responseById.getStatus());
            if (status == null) {
                status = new StatusTvSeries(responseById.getStatus());
                this.statusTvSeriesRepository.save(status);
            }
            tvSeries.setStatusTvSeries(status);
        }

        return tvSeries;
    }

    private static int getEpisodeRunTime(TvSeriesApiByIdResponseDTO responseById) {
        return (responseById.getEpisodeRuntime() == null || responseById.getEpisodeRuntime().isEmpty()) ? 0 : responseById.getEpisodeRuntime().get(0);
    }

    private StatusTvSeries findByName(String name) {
        return this.statusTvSeriesRepository.findByStatus(name).orElse(null);
    }
}


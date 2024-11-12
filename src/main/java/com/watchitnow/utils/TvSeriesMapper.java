package com.watchitnow.utils;

import com.watchitnow.database.model.dto.apiDto.TrailerResponseApiDTO;
import com.watchitnow.database.model.dto.apiDto.TvSeriesApiByIdResponseDTO;
import com.watchitnow.database.model.dto.apiDto.TvSeriesApiDTO;
import com.watchitnow.database.model.entity.TvSeries;
import com.watchitnow.service.SeriesGenreService;
import org.springframework.stereotype.Component;

@Component
public class TvSeriesMapper extends MediaMapper {

    private final SeriesGenreService seriesGenreService;

    public TvSeriesMapper(TrailerMappingUtil trailerMappingUtil, SeriesGenreService seriesGenreService) {
        super(trailerMappingUtil);
        this.seriesGenreService = seriesGenreService;
    }

    public TvSeries mapToTvSeries(TvSeriesApiDTO dto, TvSeriesApiByIdResponseDTO responseById, TrailerResponseApiDTO responseTrailer) {
        TvSeries tvSeries = new TvSeries();
        mapCommonFields(tvSeries, dto, responseTrailer);

        tvSeries.setName(dto.getName());
        tvSeries.setOriginalName(!dto.getOriginalName().equals(dto.getName()) ? dto.getOriginalName() : null);
        tvSeries.setFirstAirDate(dto.getFirstAirDate());
        tvSeries.setEpisodeRunTime(getEpisodeRunTime(responseById));
        tvSeries.setGenres(this.seriesGenreService.getAllGenresByApiIds(dto.getGenres()));

        return tvSeries;
    }

    private static int getEpisodeRunTime(TvSeriesApiByIdResponseDTO responseById) {
        return (responseById.getEpisodeRuntime() == null || responseById.getEpisodeRuntime().isEmpty()) ? 0 : responseById.getEpisodeRuntime().get(0);
    }
}


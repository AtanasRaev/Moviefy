package com.watchitnow.utils;

import com.watchitnow.database.model.dto.apiDto.MediaApiDTO;
import com.watchitnow.database.model.dto.apiDto.TrailerApiDTO;
import com.watchitnow.database.model.dto.apiDto.TrailerResponseApiDTO;
import com.watchitnow.database.model.entity.Media;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class MediaMapper {

    private final TrailerMappingUtil trailerMappingUtil;

    public MediaMapper(TrailerMappingUtil trailerMappingUtil) {
        this.trailerMappingUtil = trailerMappingUtil;
    }

    protected void mapCommonFields(Media media, MediaApiDTO dto, TrailerResponseApiDTO responseTrailer) {
        BigDecimal popularity = BigDecimal.valueOf(dto.getPopularity()).setScale(1, RoundingMode.HALF_UP);
        BigDecimal voteAverage = BigDecimal.valueOf(dto.getVoteAverage()).setScale(1, RoundingMode.HALF_UP);

        media.setApiId(dto.getId());
        media.setOverview(dto.getOverview());
        media.setPosterPath(dto.getPosterPath());
        media.setBackdropPath(dto.getBackdropPath());
        media.setPopularity(popularity.doubleValue());
        media.setVoteAverage(voteAverage.doubleValue());

        List<TrailerApiDTO> trailers = responseTrailer.getResults();
        if (!trailers.isEmpty()) {
            TrailerApiDTO selectedTrailer = this.trailerMappingUtil.getTrailer(trailers);
            if (selectedTrailer != null) {
                media.setTrailer(selectedTrailer.getKey());
            }
        }
    }
}

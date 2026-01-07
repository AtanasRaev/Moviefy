package com.moviefy.service.credit.cast;

import com.moviefy.database.model.dto.apiDto.creditDto.CastApiDTO;
import com.moviefy.database.model.dto.pageDto.creditDto.CastPageDTO;
import com.moviefy.database.model.entity.credit.cast.Cast;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;

import java.util.List;
import java.util.Set;

public interface CastService {
    void processTvSeriesCast(Set<CastApiDTO> castDto, TvSeries tvSeries);

    void processMovieCast(Set<CastApiDTO> castDto, Movie movie);

    Set<CastPageDTO> getCastByMediaId(String mediaType, long mediaId);
}

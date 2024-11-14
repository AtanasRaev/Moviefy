package com.watchitnow.service;

import com.watchitnow.database.model.dto.detailsDto.TvSeriesDetailsDTO;
import com.watchitnow.database.model.dto.pageDto.TvSeriesPageDTO;
import com.watchitnow.database.model.entity.media.StatusTvSeries;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface TvSeriesService {
    Page<TvSeriesPageDTO> getTvSeriesFromCurrentMonth(Pageable pageable);

    TvSeriesDetailsDTO getTvSeriesById(long id);

    Set<TvSeriesPageDTO> getTvSeriesByGenre(String genreType);

    StatusTvSeries findByName(String name);
}

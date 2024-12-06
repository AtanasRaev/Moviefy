package com.watchitnow.service;

import com.watchitnow.database.model.dto.detailsDto.TvSeriesDetailsDTO;
import com.watchitnow.database.model.dto.pageDto.TvSeriesPageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface TvSeriesService {
    Page<TvSeriesPageDTO> getTvSeriesFromCurrentMonth(Pageable pageable, int totalPages);

    TvSeriesDetailsDTO getTvSeriesDetailsById(long id);

    Set<TvSeriesPageDTO> getTvSeriesByGenre(String genreType);

    List<TvSeriesPageDTO> getMostPopularTvSeries(int totalItems);
}

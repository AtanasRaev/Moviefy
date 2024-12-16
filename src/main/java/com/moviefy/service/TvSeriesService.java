package com.moviefy.service;

import com.moviefy.database.model.dto.detailsDto.TvSeriesDetailsDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesPageDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesPageWithGenreDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface TvSeriesService {
    Page<TvSeriesPageDTO> getTvSeriesFromCurrentMonth(Pageable pageable, int totalPages);

    TvSeriesDetailsDTO getTvSeriesDetailsById(long id);

    Set<TvSeriesPageDTO> getTvSeriesByGenre(String genreType);

    List<TvSeriesPageDTO> getMostPopularTvSeries(int totalItems);

    List<TvSeriesPageWithGenreDTO> getBestTvSeries(int totalItems);

    boolean isEmpty();
}

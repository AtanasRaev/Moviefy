package com.moviefy.service;

import com.moviefy.database.model.dto.detailsDto.TvSeriesDetailsDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesPageDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesPageWithGenreDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface TvSeriesService {
    Page<TvSeriesPageDTO> getTvSeriesFromCurrentMonth(Pageable pageable);

    TvSeriesDetailsDTO getTvSeriesDetailsById(long id);

    Set<TvSeriesPageDTO> getTvSeriesByGenre(String genreType);

    List<TvSeriesPageDTO> getTrendingTvSeries(int totalItems);

    Page<TvSeriesPageWithGenreDTO> getPopularTvSeries(Pageable pageable);

    boolean isEmpty();
}

package com.moviefy.service;

import com.moviefy.database.model.dto.detailsDto.TvSeriesDetailsDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesPageDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesPageWithGenreDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesTrendingPageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface TvSeriesService {
    Page<TvSeriesPageDTO> getTvSeriesFromCurrentMonth(Pageable pageable);

    TvSeriesDetailsDTO getTvSeriesDetailsById(Long id);

    Set<TvSeriesPageDTO> getTvSeriesByGenre(String genreType);

    Page<TvSeriesTrendingPageDTO> getTrendingTvSeries(Pageable pageable);

    Page<TvSeriesPageWithGenreDTO> getPopularTvSeries(Pageable pageable);

    boolean isEmpty();

    List<TvSeriesTrendingPageDTO> getHomeSeriesDTO(List<String> input);
}

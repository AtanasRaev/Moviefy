package com.moviefy.service.media.tvSeries;

import com.moviefy.database.model.dto.databaseDto.EpisodeDTO;
import com.moviefy.database.model.dto.detailsDto.TvSeriesDetailsDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesPageDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesPageWithGenreDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesTrendingPageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TvSeriesService {
    Page<TvSeriesPageDTO> getTvSeriesFromCurrentMonth(Pageable pageable);

    TvSeriesDetailsDTO getTvSeriesDetailsById(Long id);

    Page<TvSeriesTrendingPageDTO> getTrendingTvSeries(Pageable pageable);

    Page<TvSeriesPageWithGenreDTO> getPopularTvSeries(Pageable pageable);

    boolean isEmpty();

    List<TvSeriesTrendingPageDTO> getHomeSeriesDTO(List<String> input);

    List<EpisodeDTO> getEpisodesFromSeason(Long seasonId);

    Integer getSeasonNumberById(Long seasonId);

    Page<TvSeriesPageWithGenreDTO> searchTvSeries(String query, Pageable pageable);

    Page<TvSeriesPageWithGenreDTO> getTvSeriesByGenres(List<String> genres, Pageable pageable);
}

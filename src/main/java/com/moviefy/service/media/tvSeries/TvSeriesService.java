package com.moviefy.service.media.tvSeries;

import com.moviefy.database.model.dto.databaseDto.EpisodeDTO;
import com.moviefy.database.model.dto.detailsDto.TvSeriesDetailsDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesPageDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesPageProjection;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesPageWithGenreDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesTrendingPageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TvSeriesService {
    Page<TvSeriesPageProjection> getTvSeriesFromCurrentMonth(Pageable pageable, List<String> genres);

    TvSeriesDetailsDTO getTvSeriesDetailsByApiId(Long apiId);

    Page<TvSeriesTrendingPageDTO> getTrendingTvSeries(Pageable pageable);

    Page<TvSeriesPageWithGenreDTO> getPopularTvSeries(Pageable pageable);

    boolean isEmpty();

    List<TvSeriesTrendingPageDTO> getHomeSeriesDTO(List<String> input);

    List<EpisodeDTO> getEpisodesFromSeason(Long seasonId);

    Integer getSeasonNumberById(Long seasonId);

    List<TvSeriesPageWithGenreDTO> searchTvSeries(String query);

    Page<TvSeriesPageWithGenreDTO> getTvSeriesByGenres(List<String> genres, Pageable pageable);

    List<String> getLowerCaseGenres(List<String> genres);
}

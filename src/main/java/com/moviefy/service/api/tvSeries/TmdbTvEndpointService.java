package com.moviefy.service.api.tvSeries;

import com.moviefy.database.model.dto.apiDto.*;

public interface TmdbTvEndpointService {
    TvSeriesResponseApiDTO getTvSeriesResponseByDateAndVoteCount(int page, int year);

    TvSeriesApiByIdResponseDTO getTvSeriesResponseById(Long apiId);

    TvSeriesResponseApiDTO searchTvSeriesQueryApi(String query);

    TvSeriesResponseApiDTO getSeriesFromToday(int page);

    TvSeriesResponseApiDTO getTrendingSeries(int page);

    EpisodesTvSeriesResponseDTO getEpisodesResponse(Long tvId, Integer season);
}

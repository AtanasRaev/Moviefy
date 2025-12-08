package com.moviefy.service.api.movie;

import com.moviefy.database.model.dto.apiDto.*;

public interface TmdbMoviesEndpointService {
    MovieResponseApiDTO getMoviesResponseByDateAndVoteCount(int page, int year);

    MovieApiByIdResponseDTO getMovieResponseById(Long apiId);

    MovieResponseApiDTO searchMoviesQueryApi(String query);

    MovieResponseApiDTO getNewMoviesUTCTime(int page);

    MovieResponseApiDTO getTrendingMovies(int page);
}

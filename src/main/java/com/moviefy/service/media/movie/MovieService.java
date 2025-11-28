package com.moviefy.service.media.movie;

import com.moviefy.database.model.dto.detailsDto.MovieDetailsDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageProjection;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageWithGenreProjection;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageWithGenreDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MovieService {
    Page<MoviePageProjection> getMoviesFromCurrentMonth(Pageable pageable, List<String> genres);

    MovieDetailsDTO getMovieDetailsByApiId(Long apiId);

    Page<MoviePageWithGenreProjection> getTrendingMovies(List<String> genres, Pageable pageable);

    Page<MoviePageWithGenreProjection> getPopularMovies(List<String> genres, Pageable pageable);

    boolean isEmpty();

    List<MoviePageWithGenreDTO> searchMovies(String query);

    Page<MoviePageProjection> getMoviesByGenres(List<String> genres, Pageable pageable);

    Page<MoviePageWithGenreProjection> getTopRatedMovies(List<String> genres, Pageable pageable);

    Page<MoviePageProjection> getMoviesByCastId(long id, Pageable pageable);
}

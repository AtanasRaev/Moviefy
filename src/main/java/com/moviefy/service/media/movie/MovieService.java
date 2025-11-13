package com.moviefy.service.media.movie;

import com.moviefy.database.model.dto.detailsDto.MovieDetailsDTO;
import com.moviefy.database.model.dto.detailsDto.MovieDetailsHomeDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.CollectionPageDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MovieHomeDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageWithGenreDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MovieService {
    Page<MoviePageDTO> getMoviesFromCurrentMonth(Pageable pageable);

    MovieDetailsDTO getMovieDetailsByApiId(Long apiId);

    Page<MoviePageWithGenreDTO> getTrendingMovies(Pageable pageable);

    Page<MoviePageWithGenreDTO> getPopularMovies(Pageable pageable);

    boolean isEmpty();

    List<MoviePageWithGenreDTO> searchMovies(String query);

    Page<MoviePageWithGenreDTO> getMoviesByGenres(List<String> genres, Pageable pageable);
}

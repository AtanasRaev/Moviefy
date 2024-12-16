package com.moviefy.service;

import com.moviefy.database.model.dto.detailsDto.MovieDetailsDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MovieDetailsHomeDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MovieHomeDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageWithGenreDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface MovieService {
    Page<MoviePageDTO> getMoviesFromCurrentMonth(Pageable pageable, int totalPages);

    MovieDetailsDTO getMovieDetailsById(Long id);

    Set<MoviePageDTO> getMoviesByGenre(String genreType);

    List<MoviePageWithGenreDTO> getMostPopularMovies(int totalItems);

    List<MoviePageWithGenreDTO> getBestMovies(int totalItems);

    boolean isEmpty();

    MovieDetailsHomeDTO findFirstMovieByCollectionName(String name);

    List<MovieHomeDTO> findMoviesByCollectionName(String name);
}

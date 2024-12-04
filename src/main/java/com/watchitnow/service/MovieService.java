package com.watchitnow.service;

import com.watchitnow.database.model.dto.detailsDto.MovieDetailsDTO;
import com.watchitnow.database.model.dto.pageDto.MoviePageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface MovieService {
    Page<MoviePageDTO> getMoviesFromCurrentMonth(Pageable pageable);

    MovieDetailsDTO getMovieDetailsById(long id);

    Set<MoviePageDTO> getMoviesByGenre(String genreType);

    Page<MoviePageDTO> getMostPopularMovies(Pageable pageable);
}

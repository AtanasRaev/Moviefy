package com.moviefy.service.genre.movieGenre;

import java.util.List;
import java.util.Set;

import com.moviefy.database.model.entity.genre.MovieGenre;

public interface MovieGenreService {
    void fetchGenres();

    boolean isEmpty();

    Set<MovieGenre> getAllGenresByApiIds(Set<Long> genreIds);

    List<MovieGenre> getAllGenresByMovieId(Long movieId);
}

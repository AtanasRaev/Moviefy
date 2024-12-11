package com.moviefy.service;

import java.util.List;
import java.util.Set;

import com.moviefy.database.model.entity.genre.SeriesGenre;

public interface SeriesGenreService {
    void fetchGenres();

    boolean isEmpty();

    Set<SeriesGenre> getAllGenresByApiIds(Set<Long> genres);

    List<SeriesGenre> getAllGenresByMovieId(Long id);
}

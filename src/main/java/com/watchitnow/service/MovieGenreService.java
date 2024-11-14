package com.watchitnow.service;

import java.util.Set;

import com.watchitnow.database.model.entity.genre.MovieGenre;

public interface MovieGenreService {
    void fetchGenres();

    boolean isEmpty();

    Set<MovieGenre> getAllGenresByApiIds(Set<Long> genreIds);
}

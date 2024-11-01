package com.watchitnow.service;

import com.watchitnow.database.model.entity.MovieGenre;

import java.util.List;
import java.util.Set;

public interface MovieGenreService {
    void fetchGenres();

    boolean isEmpty();

    Set<MovieGenre> getAllGenresByApiIds(Set<Long> genreIds);
}

package com.watchitnow.service;

import com.watchitnow.databse.model.entity.MovieGenre;

import java.util.List;

public interface MovieGenreService {
    void fetchGenres();

    boolean isEmpty();

    List<MovieGenre> getAllGenresByApiIds(List<Long> genreIds);
}

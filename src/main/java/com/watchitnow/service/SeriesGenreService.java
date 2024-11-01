package com.watchitnow.service;

import java.util.Set;

import com.watchitnow.database.model.entity.SeriesGenre;

public interface SeriesGenreService {
    void fetchGenres();

    boolean isEmpty();

    Set<SeriesGenre> getAllGenresByApiIds(Set<Long> genres);
}

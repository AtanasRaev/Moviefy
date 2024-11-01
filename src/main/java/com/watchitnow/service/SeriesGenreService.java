package com.watchitnow.service;

import com.watchitnow.database.model.entity.SeriesGenre;

import java.util.List;
import java.util.Set;

public interface SeriesGenreService {
    void fetchGenres();

    boolean isEmpty();

    Set<SeriesGenre> getAllGenresByApiIds(Set<Long> genres);
}

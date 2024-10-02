package com.watchitnow.service;

import com.watchitnow.database.model.entity.SeriesGenre;

import java.util.List;

public interface SeriesGenreService {
    void fetchGenres();

    boolean isEmpty();

    List<SeriesGenre> getAllGenresByApiIds(List<Long> genres);
}

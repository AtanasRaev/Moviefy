package com.watchitnow.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.watchitnow.database.model.entity.genre.SeriesGenre;

public interface SeriesGenreService {
    void fetchGenres();

    boolean isEmpty();

    Set<SeriesGenre> getAllGenresByApiIds(Set<Long> genres);

    List<SeriesGenre> getAllGenresByMovieId(Long id);
}

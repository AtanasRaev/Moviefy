package com.moviefy.service.genre.seriesGenre;

import java.util.List;
import java.util.Set;

import com.moviefy.database.model.entity.genre.SeriesGenre;

public interface SeriesGenreService {
    void fetchGenres();

    boolean isEmpty();

    Set<SeriesGenre> getAllGenresByApiIds(Set<Long> genresApiIds);

    List<SeriesGenre> getAllGenresByMovieId(Long id);

    List<String> getAllGenresNames();
}

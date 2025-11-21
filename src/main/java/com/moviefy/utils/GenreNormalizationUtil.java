package com.moviefy.utils;

import com.moviefy.service.genre.movieGenre.MovieGenreService;
import com.moviefy.service.genre.seriesGenre.SeriesGenreService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GenreNormalizationUtil {
    private final MovieGenreService movieGenreService;
    private final SeriesGenreService seriesGenreService;

    public GenreNormalizationUtil(MovieGenreService movieGenreService,
                                  SeriesGenreService seriesGenreService) {
        this.movieGenreService = movieGenreService;
        this.seriesGenreService = seriesGenreService;
    }

    public List<String> processMovieGenres(List<String> genres) {
        if (genres == null || genres.isEmpty()) {
            genres = this.movieGenreService.getAllGenresNames();
        }

        return getLoweredGenres(genres);
    }

    public List<String> processSeriesGenres(List<String> genres) {
        if (genres == null || genres.isEmpty()) {
            genres = this.seriesGenreService.getAllGenresNames();
        }

        genres = getSeriesLowerCaseGenres(genres);
        return genres;
    }

    public List<String> getSeriesLowerCaseGenres(List<String> genres) {
        List<String> lowerCaseGenres = getLoweredGenres(genres);

        if (lowerCaseGenres.contains("action") || lowerCaseGenres.contains("adventure")) {
            lowerCaseGenres.remove("action");
            lowerCaseGenres.remove("adventure");
            lowerCaseGenres.add("action & adventure");
        }

        if (lowerCaseGenres.contains("science fiction") || lowerCaseGenres.contains("fantasy")) {
            lowerCaseGenres.remove("science fiction");
            lowerCaseGenres.remove("fantasy");
            lowerCaseGenres.add("sci-fi & fantasy");
        }

        if (lowerCaseGenres.contains("war") || lowerCaseGenres.contains("politics")) {
            lowerCaseGenres.remove("war");
            lowerCaseGenres.remove("politics");
            lowerCaseGenres.add("war & politics");
        }
        return lowerCaseGenres;
    }

    public static List<String> getLoweredGenres(List<String> genres) {
        return new ArrayList<>(genres.stream()
                .map(String::toLowerCase)
                .toList());
    }
}

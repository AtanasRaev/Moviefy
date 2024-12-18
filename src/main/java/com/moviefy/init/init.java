package com.moviefy.init;

import com.moviefy.service.MovieGenreService;
import com.moviefy.service.SeriesGenreService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class init implements CommandLineRunner {
    private final MovieGenreService moviesGenreService;
    private final SeriesGenreService seriesGenreService;

    public init(MovieGenreService genreService,
                SeriesGenreService seriesGenreService) {
        this.moviesGenreService = genreService;
        this.seriesGenreService = seriesGenreService;
    }

    @Override
    public void run(String... args) {
        if (this.moviesGenreService.isEmpty()) {
            this.moviesGenreService.fetchGenres();
        }

        if (this.seriesGenreService.isEmpty()) {
            this.seriesGenreService.fetchGenres();
        }
    }
}

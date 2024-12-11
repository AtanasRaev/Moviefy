package com.moviefy.utils;

import com.moviefy.database.model.dto.apiDto.MovieApiByIdResponseDTO;
import com.moviefy.database.model.dto.apiDto.MovieApiDTO;
import com.moviefy.database.model.dto.apiDto.TrailerResponseApiDTO;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.service.MovieGenreService;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper extends MediaMapper {

    private final MovieGenreService genreService;

    public MovieMapper(TrailerMappingUtil trailerMappingUtil,
                       MovieGenreService genreService) {
        super(trailerMappingUtil);
        this.genreService = genreService;
    }

    public Movie mapToMovie(MovieApiDTO dto, MovieApiByIdResponseDTO responseById, TrailerResponseApiDTO responseTrailer) {
        Movie movie = new Movie();

        super.mapCommonFields(movie, dto, responseTrailer);
        movie.setTitle(dto.getTitle());
        movie.setOriginalTitle(!dto.getOriginalTitle().equals(dto.getTitle()) && !dto.getOriginalTitle().isBlank() ? dto.getOriginalTitle() : null);
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setRuntime(responseById.getRuntime());
        movie.setGenres(this.genreService.getAllGenresByApiIds(dto.getGenres()));
        movie.setGenres(this.genreService.getAllGenresByApiIds(dto.getGenres()));

        return movie;
    }
}


package com.watchitnow.utils;

import com.watchitnow.database.model.dto.apiDto.MovieApiByIdResponseDTO;
import com.watchitnow.database.model.dto.apiDto.MovieApiDTO;
import com.watchitnow.database.model.dto.apiDto.TrailerResponseApiDTO;
import com.watchitnow.database.model.entity.Movie;
import com.watchitnow.service.MovieGenreService;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper extends MediaMapper {

    private final MovieGenreService genreService;

    public MovieMapper(TrailerMappingUtil trailerMappingUtil, MovieGenreService genreService) {
        super(trailerMappingUtil);
        this.genreService = genreService;
    }

    public Movie mapToMovie(MovieApiDTO dto, MovieApiByIdResponseDTO responseById, TrailerResponseApiDTO responseTrailer) {
        Movie movie = new Movie();
        mapCommonFields(movie, dto, responseTrailer);

        movie.setTitle(dto.getTitle());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setRuntime(responseById.getRuntime());
        movie.setGenres(this.genreService.getAllGenresByApiIds(dto.getGenres()));

        return movie;
    }
}


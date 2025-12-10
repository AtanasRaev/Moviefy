package com.moviefy.utils.mappers;

import com.moviefy.database.model.dto.apiDto.mediaDto.movieDto.MovieApiByIdResponseDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.movieDto.MovieApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.TrailerResponseApiDTO;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.service.genre.movieGenre.MovieGenreService;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper extends MediaMapper {
    private final MovieGenreService genreService;

    public MovieMapper(MovieGenreService genreService) {
        this.genreService = genreService;
    }

    public Movie mapToMovie(MovieApiDTO dto, MovieApiByIdResponseDTO responseById, TrailerResponseApiDTO responseTrailer) {
        Movie movie = new Movie();

        super.mapCommonFields(movie, dto, responseTrailer);
        movie.setTitle(dto.getTitle());
        movie.setOriginalTitle(!dto.getOriginalTitle().equals(dto.getTitle()) && !dto.getOriginalTitle().isBlank() ? dto.getOriginalTitle() : null);
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setRankingYear(dto.getReleaseDate().getYear());
        movie.setAdult(dto.isAdult());
        movie.setImdbId(responseById.getImdbId() == null || responseById.getImdbId().isBlank() ? null : responseById.getImdbId());
        movie.setRuntime(responseById.getRuntime());
        movie.setGenres(this.genreService.getAllGenresByApiIds(dto.getGenres()));

        return movie;
    }
}


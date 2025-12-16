package com.moviefy.utils.mappers;

import com.moviefy.database.model.dto.apiDto.mediaDto.TrailerResponseApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.movieDto.MovieApiByIdResponseDTO;
import com.moviefy.database.model.dto.databaseDto.GenreDTO;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.service.genre.movieGenre.MovieGenreService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MovieMapper extends MediaMapper {
    private final MovieGenreService genreService;

    public MovieMapper(MovieGenreService genreService) {
        this.genreService = genreService;
    }

    public Movie mapToMovie(MovieApiByIdResponseDTO dto, TrailerResponseApiDTO responseTrailer) {
        Movie movie = new Movie();

        super.mapCommonFields(movie, dto, responseTrailer);
        movie.setTitle(dto.getTitle());
        movie.setOriginalTitle(!dto.getOriginalTitle().equals(dto.getTitle()) && !dto.getOriginalTitle().isBlank() ? dto.getOriginalTitle() : null);
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setRankingYear(dto.getReleaseDate().getYear());
        movie.setAdult(dto.isAdult());
        movie.setImdbId(dto.getImdbId() == null || dto.getImdbId().isBlank() ? null : dto.getImdbId());
        movie.setRuntime(dto.getRuntime());
        if (dto.getGenres() != null && !dto.getGenres().isEmpty()) {
            Set<Long> genreApiIds = dto.getGenres().stream()
                    .map(GenreDTO::getId)
                    .collect(Collectors.toSet());
            movie.setGenres(this.genreService.getAllGenresByApiIds(genreApiIds));
        }
        movie.setInsertedAt(dto.getReleaseDate().isBefore(LocalDate.now()) || dto.getReleaseDate().isEqual(LocalDate.now())
                ? LocalDateTime.now()
                : dto.getReleaseDate().atStartOfDay());

        return movie;
    }
}


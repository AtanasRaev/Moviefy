package com.moviefy.database.model.dto.pageDto.movieDto;

public class MoviePageWithGenreDTO extends MoviePageDTO {
    private String genre;

    private String trailer;

    public MoviePageWithGenreDTO(Long id,
                                 Double voteAverage,
                                 Double popularity,
                                 String title,
                                 Integer runtime,
                                 String posterPath,
                                 Integer year,
                                 String genre) {
        super(id, voteAverage, popularity, title, runtime, posterPath, year);
        this.genre = genre;
    }

    public MoviePageWithGenreDTO() {
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }
}

package com.moviefy.database.model.dto.pageDto.movieDto;

public class MoviePageWithGenreDTO extends MoviePageDTO {
    private String genre;

    private String trailer;

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

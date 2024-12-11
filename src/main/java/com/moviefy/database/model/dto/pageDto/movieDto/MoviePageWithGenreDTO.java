package com.moviefy.database.model.dto.pageDto.movieDto;

public class MoviePageWithGenreDTO extends MoviePageDTO {
    private String genre;

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}

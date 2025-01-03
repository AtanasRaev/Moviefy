package com.moviefy.database.model.dto.pageDto.tvSeriesDto;

public class TvSeriesPageWithGenreDTO extends TvSeriesPageDTO {
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

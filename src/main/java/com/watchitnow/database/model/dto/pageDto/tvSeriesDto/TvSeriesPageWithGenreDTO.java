package com.watchitnow.database.model.dto.pageDto.tvSeriesDto;

public class TvSeriesPageWithGenreDTO extends TvSeriesPageDTO {
    private String genre;

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}

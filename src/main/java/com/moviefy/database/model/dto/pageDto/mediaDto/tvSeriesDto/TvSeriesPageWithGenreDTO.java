package com.moviefy.database.model.dto.pageDto.mediaDto.tvSeriesDto;

public class TvSeriesPageWithGenreDTO extends TvSeriesPageDTO {
    private String genre;

    private String trailer;

    public TvSeriesPageWithGenreDTO(Long id, Double voteAverage, Double popularity, Long apiId, String name, Integer seasonsCount, Integer episodesCount, String posterPath, Integer year, String genre, String trailer) {
        super(id, voteAverage, popularity, apiId, name, seasonsCount, episodesCount, posterPath, year);
        this.genre = genre;
        this.trailer = trailer;
    }

    public TvSeriesPageWithGenreDTO() {
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

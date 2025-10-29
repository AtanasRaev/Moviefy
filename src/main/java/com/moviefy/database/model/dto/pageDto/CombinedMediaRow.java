package com.moviefy.database.model.dto.pageDto;

public class CombinedMediaRow {
    private Long id;
    private String title;
    private Double popularity;
    private String posterPath;
    private Double voteAverage;
    private Integer year;
    private String type; // "movie" or "series"
    private Integer seasonsCount;   // null for movies
    private Integer episodesCount;  // null for movies

    public CombinedMediaRow() {}

    public CombinedMediaRow(Long id, String title, Double popularity, String posterPath,
                            Double voteAverage, Integer year, String type,
                            Integer seasonsCount, Integer episodesCount) {
        this.id = id;
        this.title = title;
        this.popularity = popularity;
        this.posterPath = posterPath;
        this.voteAverage = voteAverage;
        this.year = year;
        this.type = type;
        this.seasonsCount = seasonsCount;
        this.episodesCount = episodesCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Double getPopularity() { return popularity; }
    public void setPopularity(Double popularity) { this.popularity = popularity; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public Double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getSeasonsCount() { return seasonsCount; }
    public void setSeasonsCount(Integer seasonsCount) { this.seasonsCount = seasonsCount; }

    public Integer getEpisodesCount() { return episodesCount; }
    public void setEpisodesCount(Integer episodesCount) { this.episodesCount = episodesCount; }
}

package com.moviefy.database.model.dto.pageDto.movieDto;

import com.moviefy.database.model.dto.pageDto.MediaPageDTO;

public abstract class MovieDTO extends MediaPageDTO {
    private String title;

    private Integer runtime;

    public MovieDTO(Long id, Double voteAverage, Double popularity, Long apiId, String title, Integer runtime) {
        super(id, voteAverage, popularity, apiId);
        this.title = title;
        this.runtime = runtime;
    }

    public MovieDTO() {
    }

    @Override
    public String getType() {
        return "movie";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }
}

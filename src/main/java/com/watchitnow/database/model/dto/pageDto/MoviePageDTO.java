package com.watchitnow.database.model.dto.pageDto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "title", "poster_path", "runtime"})
public class MoviePageDTO extends MediaPageDTO {
    private String title;

    private Integer runtime;

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

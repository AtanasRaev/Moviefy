package com.watchitnow.database.model.dto.pageDto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "title", "poster_path", "genres"})
public class MoviePageDTO extends MediaPageDTO {
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

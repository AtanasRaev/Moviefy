package com.moviefy.database.model.dto.pageDto;

public class CastPageDTO extends CreditPageDTO {
    private String character;

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }
}

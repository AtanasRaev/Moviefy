package com.moviefy.database.model.dto.apiDto;

public class CastApiDTO extends CreditApiDTO {
    private String character;

    private int order;

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}

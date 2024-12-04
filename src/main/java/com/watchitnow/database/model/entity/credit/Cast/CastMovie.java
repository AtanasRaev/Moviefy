package com.watchitnow.database.model.entity.credit.Cast;

import com.watchitnow.database.model.entity.credit.CreditMovie;
import jakarta.persistence.*;

@Entity
@Table(name = "cast_movies")
public class CastMovie extends CreditMovie {
    @Column
    private String character;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cast_id", referencedColumnName = "id")
    private Cast cast;

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public Cast getCast() {
        return cast;
    }

    public void setCast(Cast cast) {
        this.cast = cast;
    }
}

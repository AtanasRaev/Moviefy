package com.watchitnow.database.model.entity.credit.Cast;

import com.watchitnow.database.model.entity.credit.CreditTvSeries;
import jakarta.persistence.*;

@Entity
@Table(name = "cast_tv")
public class CastTvSeries extends CreditTvSeries {
    @Column
    private String character;

    @ManyToOne(fetch = FetchType.LAZY)
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

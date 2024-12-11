package com.moviefy.database.model.entity.credit.crew;

import com.moviefy.database.model.entity.credit.Credit;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "crew")
public class Crew extends Credit {
}

package com.moviefy.database.model.entity.credit.cast;

import com.moviefy.database.model.entity.credit.Credit;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"cast\"")
public class Cast extends Credit {
}

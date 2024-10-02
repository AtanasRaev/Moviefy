package com.watchitnow.service;

import com.watchitnow.database.model.dto.MoviePageDTO;

import java.util.List;
import java.util.Set;

public interface MovieService {
    List<MoviePageDTO> getAllByDiapason(int startYear, int endYear);

    Set<MoviePageDTO> getMoviesFromCurrentMonth(int targetCount);
}

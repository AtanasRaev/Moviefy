package com.watchitnow.service;

import com.watchitnow.databse.model.dto.MoviePageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface MovieService {
    List<MoviePageDTO> getAllByDiapason(int startYear, int endYear);

    Set<MoviePageDTO> getMoviesFromCurrentMonth(int targetCount);
}

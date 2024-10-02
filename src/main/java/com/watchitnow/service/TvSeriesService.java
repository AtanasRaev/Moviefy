package com.watchitnow.service;

import com.watchitnow.database.model.dto.pageDto.TvSeriesPageDTO;

import java.util.Set;

public interface TvSeriesService {
    Set<TvSeriesPageDTO> getTvSeriesFromCurrentMonth(int targetCount);
}

package com.moviefy.service.credit.crew;

import com.moviefy.database.model.dto.apiDto.creditDto.CrewApiDTO;
import com.moviefy.database.model.dto.pageDto.creditDto.CrewPageDTO;
import com.moviefy.database.model.entity.credit.crew.Crew;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;

import java.util.List;
import java.util.Set;

public interface CrewService {
    Set<Crew> filterAndSaveCrew(List<CrewApiDTO> crewDto);

    List<CrewApiDTO> filterCrewApiDto(Set<CrewApiDTO> crewDTO);

    void processTvSeriesCrew(Set<CrewApiDTO> crewDto, TvSeries tvSeries);

    void processMovieCrew(Set<CrewApiDTO> crewDto, Movie movie);

    Set<CrewPageDTO> getCrewByMediaId(String mediaType, long mediaId);
}

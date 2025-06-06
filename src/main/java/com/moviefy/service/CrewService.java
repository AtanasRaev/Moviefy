package com.moviefy.service;

import com.moviefy.database.model.dto.apiDto.CrewApiDTO;
import com.moviefy.database.model.dto.apiDto.MediaResponseCreditsDTO;
import com.moviefy.database.model.dto.pageDto.CrewPageDTO;
import com.moviefy.database.model.entity.credit.crew.Crew;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface CrewService {
    Set<Crew> mapToSet(List<CrewApiDTO> crewDto);

    List<CrewApiDTO> filterCrewApiDto(MediaResponseCreditsDTO creditsById);

    <T, E> void processCrew(
            List<CrewApiDTO> crewDto,
            T parentEntity,
            Function<CrewApiDTO, Optional<E>> findFunction,
            BiFunction<CrewApiDTO, T, E> entityCreator,
            Function<E, E> saveFunction,
            Function<CrewApiDTO, String> jobNameFunction,
            Set<Crew> crewSet
    );

    Set<CrewPageDTO> getCrewByMediaId(String mediaType, long mediaId);
}

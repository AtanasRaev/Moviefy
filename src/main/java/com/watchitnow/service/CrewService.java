package com.watchitnow.service;

import com.watchitnow.database.model.dto.apiDto.CrewApiApiDTO;
import com.watchitnow.database.model.dto.apiDto.MediaResponseCreditsDTO;
import com.watchitnow.database.model.entity.credit.Crew.Crew;
import com.watchitnow.database.model.entity.credit.Crew.JobCrew;
import com.watchitnow.database.model.entity.media.Media;

import java.util.List;
import java.util.Set;

public interface CrewService {
    List<Crew> findAllByApiId(List<Long> apiIds);

    void saveAll(Set<Crew> crews);

    Set<Crew> mapToSet(List<CrewApiApiDTO> crewDto, Media media);

    List<CrewApiApiDTO> filterCrewApiDto(MediaResponseCreditsDTO creditsById);

    JobCrew findJobByName(String name);

    void saveJob(JobCrew jobByName);
}

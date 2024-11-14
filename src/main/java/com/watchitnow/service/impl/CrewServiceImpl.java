package com.watchitnow.service.impl;

import com.watchitnow.database.model.dto.apiDto.CreditApiDTO;
import com.watchitnow.database.model.dto.apiDto.CrewApiApiDTO;
import com.watchitnow.database.model.dto.apiDto.MediaResponseCreditsDTO;
import com.watchitnow.database.model.entity.credit.Credit;
import com.watchitnow.database.model.entity.credit.Crew.Crew;
import com.watchitnow.database.model.entity.credit.Crew.JobCrew;
import com.watchitnow.database.model.entity.media.Media;
import com.watchitnow.database.repository.CrewRepository;
import com.watchitnow.database.repository.JobCrewRepository;
import com.watchitnow.service.CrewService;
import com.watchitnow.utils.CreditRetrievalUtil;
import com.watchitnow.utils.CrewMapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class CrewServiceImpl implements CrewService {
    private final CrewRepository crewRepository;
    private final JobCrewRepository jobCrewRepository;
    private final CreditRetrievalUtil creditRetrievalUtil;
    private final CrewMapper crewMapper;

    public CrewServiceImpl(CrewRepository crewRepository,
                           JobCrewRepository jobCrewRepository,
                           CreditRetrievalUtil creditRetrievalUtil,
                           CrewMapper crewMapper) {
        this.crewRepository = crewRepository;
        this.jobCrewRepository = jobCrewRepository;
        this.creditRetrievalUtil = creditRetrievalUtil;
        this.crewMapper = crewMapper;
    }

    @Override
    public List<Crew> findAllByApiId(List<Long> apiIds) {
        return this.crewRepository.findAllByApiIds(apiIds);
    }

    @Override
    public void saveAll(Set<Crew> crews) {
        this.crewRepository.saveAll(crews);
    }

    @Override
    public Set<Crew> mapToSet(List<CrewApiApiDTO> crewDto, Media movie) {
        return creditRetrievalUtil.creditRetrieval(
                crewDto,
                crew -> this.crewMapper.mapToCrew(crew, movie),
                CreditApiDTO::getId,
                Credit::getApiId,
                this::findAllByApiId,
                savedCrew -> {
                    this.saveAll(savedCrew);
                    return null;
                }
        );
    }

    @Override
    public List<CrewApiApiDTO> filterCrewApiDto(MediaResponseCreditsDTO creditsById) {
        return creditsById.getCrew()
                .stream()
                .sorted(Comparator.comparing(CrewApiApiDTO::getPopularity).reversed())
                .filter(crew ->
                        (crew.getJob() != null &&
                                (crew.getJob().equals("Director")
                                        || crew.getJob().equals("Writer")
                                        || crew.getJob().equals("Novel")
                                        || crew.getJob().equals("Screenplay")
                                        || crew.getJob().equals("Producer")))
                                && crew.getName() != null && !crew.getName().isBlank()
                )
                .limit(6)
                .toList();
    }

    @Override
    public JobCrew findJobByName(String name) {
        return this.jobCrewRepository.findByJob(name).orElse(null);
    }

    @Override
    public void saveJob(JobCrew jobByName) {
        this.jobCrewRepository.save(jobByName);
    }
}

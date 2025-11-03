package com.moviefy.service.credit.crew;

import com.moviefy.database.model.dto.apiDto.CreditApiDTO;
import com.moviefy.database.model.dto.apiDto.CrewApiDTO;
import com.moviefy.database.model.dto.apiDto.MediaResponseCreditsDTO;
import com.moviefy.database.model.dto.pageDto.CrewPageDTO;
import com.moviefy.database.model.entity.credit.Credit;
import com.moviefy.database.model.entity.credit.crew.Crew;
import com.moviefy.database.model.entity.credit.crew.CrewMovie;
import com.moviefy.database.model.entity.credit.crew.CrewTvSeries;
import com.moviefy.database.model.entity.credit.crew.JobCrew;
import com.moviefy.database.repository.credit.crew.CrewMovieRepository;
import com.moviefy.database.repository.credit.crew.CrewRepository;
import com.moviefy.database.repository.credit.crew.CrewTvSeriesRepository;
import com.moviefy.database.repository.credit.crew.JobCrewRepository;
import com.moviefy.utils.CreditRetrievalUtil;
import com.moviefy.utils.CrewMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class CrewServiceImpl implements CrewService {
    private final CrewRepository crewRepository;
    private final CrewMovieRepository crewMovieRepository;
    private final CrewTvSeriesRepository crewTvSeriesRepository;
    private final JobCrewRepository jobCrewRepository;
    private final CreditRetrievalUtil creditRetrievalUtil;
    private final CrewMapper crewMapper;

    public CrewServiceImpl(CrewRepository crewRepository,
                           CrewMovieRepository crewMovieRepository,
                           CrewTvSeriesRepository crewTvSeriesRepository,
                           JobCrewRepository jobCrewRepository,
                           CreditRetrievalUtil creditRetrievalUtil,
                           CrewMapper crewMapper) {
        this.crewRepository = crewRepository;
        this.crewMovieRepository = crewMovieRepository;
        this.crewTvSeriesRepository = crewTvSeriesRepository;
        this.jobCrewRepository = jobCrewRepository;
        this.creditRetrievalUtil = creditRetrievalUtil;
        this.crewMapper = crewMapper;
    }

    @Override
    public Set<Crew> mapToSet(List<CrewApiDTO> crewDto) {
        return creditRetrievalUtil.creditRetrieval(
                crewDto,
                this.crewMapper::mapToCrew,
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
    public List<CrewApiDTO> filterCrewApiDto(MediaResponseCreditsDTO creditsById) {
        Map<Long, String> uniqueIds = new HashMap<>();
        return creditsById.getCrew()
                .stream()
                .sorted(Comparator.comparing(CrewApiDTO::getPopularity).reversed())
                .filter(crew ->
                        (crew.getJob() != null
                                && (crew.getJob().equals("Director")
                                || crew.getJob().equals("Writer")
                                || crew.getJob().equals("Novel")
                                || crew.getJob().equals("Screenplay")
                                || crew.getJob().equals("Producer")))
                                && crew.getName() != null
                                && !crew.getName().isBlank()
                )
                .filter(crew -> {
                    if (uniqueIds.containsKey(crew.getId()) && uniqueIds.get(crew.getId()).equals(crew.getJob())) {
                        return false;
                    }
                    uniqueIds.put(crew.getId(), crew.getJob());
                    return true;
                })
                .limit(6)
                .toList();
    }

    @Override
    public <T, E> void processCrew(
            List<CrewApiDTO> crewDto,
            T parentEntity,
            Function<CrewApiDTO, Optional<E>> findFunction,
            BiFunction<CrewApiDTO, T, E> entityCreator,
            Function<E, E> saveFunction,
            Function<CrewApiDTO, String> jobNameFunction,
            Set<Crew> crewSet
    ) {
        crewDto.forEach(c -> {
            Optional<E> optional = findFunction.apply(c);
            if (optional.isEmpty()) {
                String jobName = jobNameFunction.apply(c);
                JobCrew job = findOrCreateJob(jobName);

                E entity = entityCreator.apply(c, parentEntity);
                assignCrewAndJob(entity, crewSet, c.getId(), job);
                saveFunction.apply(entity);
            }
        });
    }

    @Override
    public Set<CrewPageDTO> getCrewByMediaId(String mediaType, long mediaId) {
        Set<CrewPageDTO> crewPageDTOs;
        if ("movie".equals(mediaType)) {
            crewPageDTOs = new LinkedHashSet<>(this.creditRetrievalUtil.getCreditByMediaId(mediaId,
                    CrewPageDTO::new,
                    crewMovieRepository::findCrewByMovieId,
                    CrewPageDTO::setId,
                    CrewPageDTO::setJob,
                    CrewPageDTO::setName,
                    CrewPageDTO::setProfilePath,
                    CrewMovie::getId,
                    cm -> cm.getJob().getJob(),
                    cm -> cm.getCrew().getName(),
                    cm -> cm.getCrew().getProfilePath())
            );
        } else if ("tv".equals(mediaType)) {
            crewPageDTOs = new LinkedHashSet<>(this.creditRetrievalUtil.getCreditByMediaId(mediaId,
                    CrewPageDTO::new,
                    crewTvSeriesRepository::findCrewByTvSeriesId,
                    CrewPageDTO::setId,
                    CrewPageDTO::setJob,
                    CrewPageDTO::setName,
                    CrewPageDTO::setProfilePath,
                    CrewTvSeries::getId,
                    cm -> cm.getJob().getJob(),
                    cm -> cm.getCrew().getName(),
                    cm -> cm.getCrew().getProfilePath())

            );
        } else {
            throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        }

        return crewPageDTOs;
    }

    private JobCrew findOrCreateJob(String jobName) {
        Optional<JobCrew> optional = jobCrewRepository.findByJob(jobName);
        if (optional.isEmpty()) {
            JobCrew job = new JobCrew(jobName);
            jobCrewRepository.save(job);
            return job;
        }
        return optional.get();
    }

    private <E> void assignCrewAndJob(E entity, Set<Crew> crewSet, long crewApiId, JobCrew job) {
        Crew crew = crewSet.stream()
                .filter(c -> c.getApiId() == crewApiId)
                .findFirst()
                .orElse(null);

        if (entity instanceof CrewMovie) {
            ((CrewMovie) entity).setCrew(crew);
            ((CrewMovie) entity).setJob(job);
        } else if (entity instanceof CrewTvSeries) {
            ((CrewTvSeries) entity).setCrew(crew);
            ((CrewTvSeries) entity).setJob(job);
        }
    }

    private List<Crew> findAllByApiId(List<Long> apiIds) {
        return this.crewRepository.findAllByApiIds(apiIds);
    }

    private void saveAll(Set<Crew> crews) {
        this.crewRepository.saveAll(crews);
    }
}

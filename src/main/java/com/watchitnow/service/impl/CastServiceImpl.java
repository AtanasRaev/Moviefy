package com.watchitnow.service.impl;

import com.watchitnow.database.model.dto.apiDto.CastApiApiDTO;
import com.watchitnow.database.model.dto.apiDto.CreditApiDTO;
import com.watchitnow.database.model.dto.apiDto.MediaResponseCreditsDTO;
import com.watchitnow.database.model.dto.pageDto.CastPageDTO;
import com.watchitnow.database.model.entity.credit.cast.Cast;
import com.watchitnow.database.model.entity.credit.cast.CastMovie;
import com.watchitnow.database.model.entity.credit.cast.CastTvSeries;
import com.watchitnow.database.model.entity.credit.Credit;
import com.watchitnow.database.repository.CastMovieRepository;
import com.watchitnow.database.repository.CastRepository;
import com.watchitnow.database.repository.CastTvSeriesRepository;
import com.watchitnow.service.CastService;
import com.watchitnow.utils.CastMapper;
import com.watchitnow.utils.CreditRetrievalUtil;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class CastServiceImpl implements CastService {
    private final CastRepository castRepository;
    private final CastMovieRepository castMovieRepository;
    private final CastTvSeriesRepository castTvSeriesRepository;
    private final CreditRetrievalUtil creditRetrievalUtil;
    private final CastMapper castMapper;

    public CastServiceImpl(CastRepository castRepository,
                           CastMovieRepository castMovieRepository,
                           CreditRetrievalUtil creditRetrievalUtil,
                           CastMapper castMapper, CastTvSeriesRepository castTvSeriesRepository) {
        this.castRepository = castRepository;
        this.castMovieRepository = castMovieRepository;
        this.creditRetrievalUtil = creditRetrievalUtil;
        this.castMapper = castMapper;
        this.castTvSeriesRepository = castTvSeriesRepository;
    }

    @Override
    public Set<Cast> mapToSet(List<CastApiApiDTO> castDto) {
        return creditRetrievalUtil.creditRetrieval(
                castDto,
                this.castMapper::mapToCast,
                CreditApiDTO::getId,
                Credit::getApiId,
                this::findAllByApiId,
                savedCast -> {
                    this.saveAll(savedCast);
                    return null;
                }
        );
    }

    @Override
    public List<CastApiApiDTO> filterCastApiDto(MediaResponseCreditsDTO creditsById) {
        Map<Long, String> uniqueIds = new HashMap<>();
        return creditsById.getCast()
                .stream()
                .filter(cast -> cast.getName() != null && !cast.getName().isBlank()
                        && cast.getCharacter() != null && !cast.getCharacter().isBlank())
                .filter(cast -> {
                    if (uniqueIds.containsKey(cast.getId()) && uniqueIds.get(cast.getId()).equals(cast.getCharacter())) {
                        return false;
                    }
                    uniqueIds.put(cast.getId(), cast.getCharacter());
                    return true;
                })
                .sorted(Comparator.comparing(CastApiApiDTO::getOrder))
                .limit(10)
                .toList();
    }

    @Override
    public <T, E> void processCast(
            List<CastApiApiDTO> castDto,
            T parentEntity,
            Function<CastApiApiDTO, Optional<E>> findFunction,
            BiFunction<CastApiApiDTO, T, E> entityCreator,
            Function<E, E> saveFunction
    ) {
        castDto.forEach(c -> {
            Optional<E> optional = findFunction.apply(c);
            if (optional.isEmpty()) {
                E entity = entityCreator.apply(c, parentEntity);
                saveFunction.apply(entity);
            }
        });
    }

    @Override
    public <T, E> E createCastEntity(
            CastApiApiDTO dto,
            T parentEntity,
            Set<Cast> castSet,
            Supplier<E> entityCreator,
            BiConsumer<E, T> setParentFunction,
            BiConsumer<E, Cast> setCastFunction,
            BiConsumer<E, String> setCharacterFunction
    ) {
        E entity = entityCreator.get(); // Use no-arg constructor
        setParentFunction.accept(entity, parentEntity);

        Cast cast = castSet.stream()
                .filter(c -> c.getApiId() == dto.getId())
                .findFirst()
                .orElse(null);

        setCastFunction.accept(entity, cast);
        setCharacterFunction.accept(entity, sanitizeCharacter(dto.getCharacter()));

        return entity;
    }

    @Override
    public Set<CastPageDTO> getCastByMediaId(String mediaType, long mediaId) {
        Set<CastPageDTO> castPageDTOs;
        if ("movie".equals(mediaType)) {
            castPageDTOs = new LinkedHashSet<>(this.creditRetrievalUtil.getCreditByMediaId(mediaId,
                    CastPageDTO::new,
                    castMovieRepository::findCastByMovieId,
                    CastPageDTO::setId,
                    CastPageDTO::setCharacter,
                    CastPageDTO::setName,
                    CastPageDTO::setProfilePath,
                    CastMovie::getId,
                    CastMovie::getCharacter,
                    cm -> cm.getCast().getName(),
                    cm -> cm.getCast().getProfilePath())
            );
        } else if ("tv".equals(mediaType)) {
            castPageDTOs = new LinkedHashSet<>(this.creditRetrievalUtil.getCreditByMediaId(mediaId,
                    CastPageDTO::new,
                    castTvSeriesRepository::findCastByTvSeriesId,
                    CastPageDTO::setId,
                    CastPageDTO::setCharacter,
                    CastPageDTO::setName,
                    CastPageDTO::setProfilePath,
                    CastTvSeries::getId,
                    CastTvSeries::getCharacter,
                    cm -> cm.getCast().getName(),
                    cm -> cm.getCast().getProfilePath())
            );
        } else {
            throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        }

        return castPageDTOs;
    }

    private String sanitizeCharacter(String character) {
        return (character == null || character.isBlank() || character.length() > 255) ? null : character;
    }

    private List<Cast> findAllByApiId(List<Long> apiIds) {
        return this.castRepository.findAllByApiIds(apiIds);
    }

    private void saveAll(Set<Cast> cast) {
        this.castRepository.saveAll(cast);
    }
}

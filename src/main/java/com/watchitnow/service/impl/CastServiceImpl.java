package com.watchitnow.service.impl;

import com.watchitnow.database.model.dto.apiDto.CastApiApiDTO;
import com.watchitnow.database.model.dto.apiDto.CreditApiDTO;
import com.watchitnow.database.model.dto.apiDto.MediaResponseCreditsDTO;
import com.watchitnow.database.model.entity.credit.Cast.Cast;
import com.watchitnow.database.model.entity.credit.Credit;
import com.watchitnow.database.model.entity.media.Media;
import com.watchitnow.database.repository.CastRepository;
import com.watchitnow.service.CastService;
import com.watchitnow.utils.CastMapper;
import com.watchitnow.utils.CreditRetrievalUtil;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.*;

@Service
public class CastServiceImpl implements CastService {
    private final CastRepository castRepository;
    private final CreditRetrievalUtil creditRetrievalUtil;
    private final CastMapper castMapper;

    public CastServiceImpl(CastRepository castRepository,
                           CreditRetrievalUtil creditRetrievalUtil,
                           CastMapper castMapper) {
        this.castRepository = castRepository;
        this.creditRetrievalUtil = creditRetrievalUtil;
        this.castMapper = castMapper;
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

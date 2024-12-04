package com.watchitnow.service;

import com.watchitnow.database.model.dto.apiDto.CastApiApiDTO;
import com.watchitnow.database.model.dto.apiDto.MediaResponseCreditsDTO;
import com.watchitnow.database.model.dto.pageDto.CastPageDTO;
import com.watchitnow.database.model.entity.credit.Cast.Cast;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface CastService {
    Set<Cast> mapToSet(List<CastApiApiDTO> castDto);

    List<CastApiApiDTO> filterCastApiDto(MediaResponseCreditsDTO creditsById);

    <T, E> void processCast(
            List<CastApiApiDTO> castDto,
            T parentEntity,
            Function<CastApiApiDTO, Optional<E>> findFunction,
            BiFunction<CastApiApiDTO, T, E> entityCreator,
            Function<E, E> saveFunction
    );

    <T, E> E createCastEntity(
            CastApiApiDTO dto,
            T parentEntity,
            Set<Cast> castSet,
            Supplier<E> entityCreator,
            BiConsumer<E, T> setParentFunction,
            BiConsumer<E, Cast> setCastFunction,
            BiConsumer<E, String> setCharacterFunction
    );

    Set<CastPageDTO> getCastByMediaId(String mediaType, long mediaId);
}

package com.moviefy.service.credit.cast;

import com.moviefy.database.model.dto.apiDto.CastApiDTO;
import com.moviefy.database.model.dto.pageDto.CastPageDTO;
import com.moviefy.database.model.entity.credit.cast.Cast;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface CastService {
    Set<Cast> mapToSet(List<CastApiDTO> castDto);

    List<CastApiDTO> filterCastApiDto(Set<CastApiDTO> castDTO);

    <T, E> void processCast(
            List<CastApiDTO> castDto,
            T parentEntity,
            Function<CastApiDTO, Optional<E>> findFunction,
            BiFunction<CastApiDTO, T, E> entityCreator,
            Function<E, E> saveFunction
    );

    <T, E> E createCastEntity(
            CastApiDTO dto,
            T parentEntity,
            Set<Cast> castSet,
            Supplier<E> entityCreator,
            BiConsumer<E, T> setParentFunction,
            BiConsumer<E, Cast> setCastFunction,
            BiConsumer<E, String> setCharacterFunction
    );

    Set<CastPageDTO> getCastByMediaId(String mediaType, long mediaId);
}

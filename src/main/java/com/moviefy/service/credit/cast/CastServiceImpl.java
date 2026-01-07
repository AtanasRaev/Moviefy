package com.moviefy.service.credit.cast;

import com.moviefy.database.model.dto.apiDto.creditDto.CastApiDTO;
import com.moviefy.database.model.dto.apiDto.creditDto.CreditApiDTO;
import com.moviefy.database.model.dto.pageDto.creditDto.CastPageDTO;
import com.moviefy.database.model.entity.credit.Credit;
import com.moviefy.database.model.entity.credit.cast.Cast;
import com.moviefy.database.model.entity.credit.cast.CastMovie;
import com.moviefy.database.model.entity.credit.cast.CastTvSeries;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;
import com.moviefy.database.repository.credit.cast.CastMovieRepository;
import com.moviefy.database.repository.credit.cast.CastRepository;
import com.moviefy.database.repository.credit.cast.CastTvSeriesRepository;
import com.moviefy.utils.mappers.CastMapper;
import com.moviefy.utils.CreditRetrievalUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void processTvSeriesCast(Set<CastApiDTO> castDTO, TvSeries tvSeries) {
        List<CastApiDTO> castList = this.filterCastApiDto(castDTO);
        Set<Cast> castSet = this.filterAndSave(castList);

        this.processCast(
                castList,
                tvSeries,
                c -> castTvSeriesRepository.findByTvSeriesIdAndCastApiIdAndCharacter(tvSeries.getId(), c.getId(), c.getCharacter()),
                (c, t) -> this.createCastEntity(
                        c,
                        t,
                        castSet,
                        CastTvSeries::new,
                        CastTvSeries::setTvSeries,
                        CastTvSeries::setCast,
                        CastTvSeries::setCharacter
                ),
                castTvSeriesRepository::save
        );
    }

    @Override
    public void processMovieCast(Set<CastApiDTO> castDto, Movie movie) {
        List<CastApiDTO> castList = this.filterCastApiDto(castDto);
        Set<Cast> castSet = this.filterAndSave(castList);

        this.processCast(
                castList,
                movie,
                c -> castMovieRepository.findByMovieIdAndCastApiIdAndCharacter(movie.getId(), c.getId(), c.getCharacter()),
                (c, m) -> this.createCastEntity(
                        c,
                        m,
                        castSet,
                        CastMovie::new,
                        CastMovie::setMovie,
                        CastMovie::setCast,
                        CastMovie::setCharacter
                ),
                castMovieRepository::save
        );
    }

    @Override
    @Transactional(readOnly = true)
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
                    CastMovie::getCastId,
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
                    CastTvSeries::getCastId,
                    CastTvSeries::getCharacter,
                    cm -> cm.getCast().getName(),
                    cm -> cm.getCast().getProfilePath())
            );
        } else {
            throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        }

        return castPageDTOs;
    }

    private  <T, E> void processCast(
            List<CastApiDTO> castDto,
            T parentEntity,
            Function<CastApiDTO, Optional<E>> findFunction,
            BiFunction<CastApiDTO, T, E> entityCreator,
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

    private  <T, E> E createCastEntity(
            CastApiDTO dto,
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

    private Set<Cast> filterAndSave(List<CastApiDTO> castDto) {
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

    private List<CastApiDTO> filterCastApiDto(Set<CastApiDTO> castDTO) {
        Map<Long, String> uniqueIds = new HashMap<>();
        return castDTO
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
                .sorted(Comparator.comparing(CastApiDTO::getOrder))
                .limit(10)
                .toList();
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

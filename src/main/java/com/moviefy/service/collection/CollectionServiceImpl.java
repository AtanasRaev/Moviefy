package com.moviefy.service.collection;

import com.moviefy.config.cache.CacheKeys;
import com.moviefy.database.model.dto.apiDto.CollectionApiDTO;
import com.moviefy.database.model.dto.detailsDto.MovieDetailsHomeDTO;
import com.moviefy.database.model.dto.pageDto.creditDto.CrewHomePageDTO;
import com.moviefy.database.model.dto.pageDto.creditDto.CrewPageDTO;
import com.moviefy.database.model.dto.pageDto.ProductionHomePageDTO;
import com.moviefy.database.model.dto.pageDto.CollectionPageDTO;
import com.moviefy.database.model.dto.pageDto.CollectionPageProjection;
import com.moviefy.database.model.dto.pageDto.mediaDto.movieDto.MovieHomeDTO;
import com.moviefy.database.model.dto.pageDto.mediaDto.movieDto.MoviePageDTO;
import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.media.Collection;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.database.repository.media.CollectionRepository;
import com.moviefy.service.credit.crew.CrewService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class CollectionServiceImpl implements CollectionService {
    private final CollectionRepository collectionRepository;
    private final CrewService crewService;
    private final ModelMapper modelMapper;

    public CollectionServiceImpl(CollectionRepository collectionRepository,
                                 CrewService crewService,
                                 ModelMapper modelMapper) {
        this.collectionRepository = collectionRepository;
        this.crewService = crewService;
        this.modelMapper = modelMapper;
    }

    @Override
    public void saveCollection(Collection collection) {
        this.collectionRepository.save(collection);
    }

    @Override
    public Collection findByApiId(long apiId) {
        return this.collectionRepository.findByApiId(apiId)
                .orElse(null);
    }

    @Override
    public Collection getCollectionFromResponse(CollectionApiDTO collectionDto, Movie movie) {
        Collection collection = findByApiId(collectionDto.getId());

        if (collection == null) {
            collection = new Collection(
                    collectionDto.getId(),
                    collectionDto.getName(),
                    collectionDto.getPosterPath()
            );
        }

        collection.getMovies().add(movie);

        if (collection.getMovies().size() > 1) {
            collection.setHasMovies(true);
        }

        double averageVoteCount = collection.getMovies()
                .stream()
                .mapToInt(Movie::getVoteCount)
                .average()
                .orElse(0);

        BigDecimal voteAverage = BigDecimal.valueOf(averageVoteCount).setScale(1, RoundingMode.HALF_UP);

        collection.setVoteCountAverage(voteAverage.doubleValue());
        saveCollection(collection);

        return collection;
    }

    @Override
    public Collection getCollectionByMovieId(Long movieId) {
        return this.collectionRepository.findCollectionsByMovieId(movieId)
                .orElse(null);
    }

    @Override
    public List<Collection> getByName(String name) {
        return this.collectionRepository.findByName(name);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.COLLECTIONS_BY_NAME,
            key = "#input",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<CollectionPageDTO> getCollectionsByName(List<String> input) {
        return this.collectionRepository.findAllByNameIn(input).stream()
                .map(this::mapCollectionPageDTO)
                .toList();
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.MOVIES_BY_API_ID,
            key = "#apiId",
            unless = "#result == null || #result.isEmpty()"
    )
    public Map<String, List<MoviePageDTO>> getMoviesByApiId(Long apiId) {
        Collection collection = this.findByApiId(apiId);
        return Map.of(collection.getName(), collection.getMovies().stream()
                .sorted(Comparator.comparing(Movie::getReleaseDate))
                .map(movie -> {
                    MoviePageDTO dto = this.modelMapper.map(movie, MoviePageDTO.class);
                    dto.setYear(movie.getReleaseDate().getYear());
                    return dto;
                })
                .toList());
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.POPULAR_COLLECTIONS,
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()",
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<CollectionPageProjection> getPopular(Pageable pageable) {
        return this.collectionRepository.findAllByVoteCountAverageDesc(pageable);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.MOVIES_HOME_BY_COLLECTION,
            key = "#input",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public Map<String, Object> getMoviesHomeByCollection(String input) {
        List<Collection> collections = this.getByName(input);

        if (collections.isEmpty()) {
            return null;
        }

        Collection collection = collections.get(0);
        Set<Movie> movies = collection.getMovies();

        Map<String, Object> result = new HashMap<>();

        movies.stream()
                .min(Comparator.comparing(Movie::getReleaseDate))
                .ifPresent(movie -> result.put("first_movie", mapFirstMovie(movie)));

        List<MovieHomeDTO> restMovies = movies.size() > 1
                ? movies.stream()
                .sorted(Comparator.comparing(Movie::getReleaseDate))
                .map(movie -> this.modelMapper.map(movie, MovieHomeDTO.class))
                .skip(1)
                .toList()
                : List.of();

        result.put("rest_movies", restMovies);
        result.put("collection_api_id", collection.getApiId());

        return result;
    }

    @Override
    public Page<CollectionPageProjection> searchCollections(String query, Pageable pageable) {
        return this.collectionRepository.searchCollectionByName(query, pageable);
    }

    private MovieDetailsHomeDTO mapFirstMovie(Movie movie) {
        MovieDetailsHomeDTO movieDTO = this.modelMapper.map(movie, MovieDetailsHomeDTO.class);

        movieDTO.setYear(movie.getReleaseDate().getYear());

        movieDTO.setProductionCompany(
                movie.getProductionCompanies().stream()
                        .sorted(Comparator.comparing(ProductionCompany::getId))
                        .map(company -> this.modelMapper.map(company, ProductionHomePageDTO.class))
                        .toList()
        );

        movieDTO.setCrew(
                this.crewService.getCrewByMediaId("movie", movie.getId()).stream()
                        .sorted(Comparator.comparing(CrewPageDTO::getId))
                        .map(crew -> this.modelMapper.map(crew, CrewHomePageDTO.class))
                        .toList()
        );

        return movieDTO;
    }

    private CollectionPageDTO mapCollectionPageDTO(Collection collection) {
        CollectionPageDTO map = this.modelMapper.map(collection, CollectionPageDTO.class);
        collection.getMovies()
                .stream()
                .min(Comparator.comparing(Movie::getReleaseDate))
                .stream()
                .findFirst()
                .ifPresent(m -> {
                    map.setOverview(m.getOverview());
                    map.setRuntime(m.getRuntime());
                    map.setVoteAverage(m.getVoteAverage());
                });
        map.setVoteAverage(
                Math.round(
                        collection.getMovies()
                                .stream()
                                .mapToDouble(Movie::getVoteAverage)
                                .average()
                                .orElse(0) * 100.0
                ) / 100.0
        );

        return map;
    }
}

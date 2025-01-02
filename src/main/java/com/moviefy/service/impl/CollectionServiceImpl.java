package com.moviefy.service.impl;

import com.moviefy.database.model.dto.apiDto.CollectionApiDTO;
import com.moviefy.database.model.entity.media.Collection;
import com.moviefy.database.model.entity.media.Media;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.database.repository.CollectionRepository;
import com.moviefy.service.CollectionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CollectionServiceImpl implements CollectionService {
    private final CollectionRepository collectionRepository;

    public CollectionServiceImpl(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
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
    public List<Collection> getByNames(List<String> name) {
        return this.collectionRepository.findAllByNameIn(name);
    }
}

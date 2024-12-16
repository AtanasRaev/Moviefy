package com.moviefy.service.impl;

import com.moviefy.database.model.dto.apiDto.CollectionApiDTO;
import com.moviefy.database.model.entity.Collection;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.database.repository.CollectionRepository;
import com.moviefy.service.CollectionService;
import org.springframework.stereotype.Service;

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
            collection = new Collection(collectionDto.getId(), collectionDto.getName(), collectionDto.getPosterPath());
        }

        collection.getMovies().add(movie);
        saveCollection(collection);

        return collection;
    }

    @Override
    public List<Collection> findByName(String name) {
        return this.collectionRepository.findByName(name);
    }
}

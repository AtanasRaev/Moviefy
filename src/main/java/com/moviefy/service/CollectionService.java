package com.moviefy.service;

import com.moviefy.database.model.dto.apiDto.CollectionApiDTO;
import com.moviefy.database.model.entity.Collection;
import com.moviefy.database.model.entity.media.Movie;

import java.util.List;

public interface CollectionService {
    void saveCollection(Collection collection);

    Collection findByApiId(long apiId);

    Collection getCollectionFromResponse(CollectionApiDTO collectionDto, Movie movie);

    Collection getCollectionByMovieId(Long movieId);

    List<Collection> getByName(String name);

    List<Collection> getByNames(List<String> name);
}

package com.moviefy.service.collection;

import com.moviefy.database.model.dto.apiDto.CollectionApiDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.CollectionPageDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageDTO;
import com.moviefy.database.model.entity.media.Collection;
import com.moviefy.database.model.entity.media.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface CollectionService {
    void saveCollection(Collection collection);

    Collection findByApiId(long apiId);

    Collection getCollectionFromResponse(CollectionApiDTO collectionDto, Movie movie);

    Collection getCollectionByMovieId(Long movieId);

    List<Collection> getByName(String name);

    List<CollectionPageDTO> getCollectionsByName(List<String> input);

    Map<String, List<MoviePageDTO>> getMoviesByApiId(Long apiId);

    Page<CollectionPageDTO> getPopular(Pageable pageable);

    Map<String, Object> getMoviesHomeByCollection(String input);
}

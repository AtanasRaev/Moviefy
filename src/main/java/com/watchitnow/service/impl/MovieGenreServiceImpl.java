package com.watchitnow.service.impl;

import com.watchitnow.config.ApiConfig;
import com.watchitnow.database.model.dto.apiDto.GenreResponseApiDTO;
import com.watchitnow.database.model.entity.MovieGenre;
import com.watchitnow.database.repository.MovieGenreRepository;
import com.watchitnow.service.MovieGenreService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class MovieGenreServiceImpl implements MovieGenreService {
    private final MovieGenreRepository genreRepository;
    private final ApiConfig apiConfig;
    private final RestClient restClient;

    public MovieGenreServiceImpl(MovieGenreRepository genreRepository,
                                 ApiConfig apiConfig,
                                 RestClient restClient
    ) {
        this.genreRepository = genreRepository;
        this.apiConfig = apiConfig;
        this.restClient = restClient;
    }


    @Override
    public void fetchGenres() {
        GenreResponseApiDTO response = getResponse();
        List<MovieGenre> genres = response
                .getGenres()
                .stream()
                .map(dto -> new MovieGenre(dto.getName(), dto.getId()))
                .toList();
        this.genreRepository.saveAll(genres);
    }

    @Override
    public boolean isEmpty() {
        return this.genreRepository.count() == 0;
    }

    @Override
    public Set<MovieGenre> getAllGenresByApiIds(Set<Long> genreIds) {
        Set<MovieGenre> genresList = new HashSet<>();
        for (Long genre : genreIds) {
            Optional<MovieGenre> optional = this.genreRepository.findByApiId(genre);
            optional.ifPresent(genresList::add);
        }
        return genresList;
    }

    private GenreResponseApiDTO getResponse() {
        String url = this.apiConfig.getUrl() + "/genre/movie/list?api_key=" + this.apiConfig.getKey();

        return this.restClient
                .get()
                .uri(url)
                .retrieve()
                .body(GenreResponseApiDTO.class);
    }
}

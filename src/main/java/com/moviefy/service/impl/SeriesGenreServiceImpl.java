package com.moviefy.service.impl;

import com.moviefy.config.ApiConfig;
import com.moviefy.database.model.dto.apiDto.GenreResponseApiDTO;
import com.moviefy.database.model.entity.genre.SeriesGenre;
import com.moviefy.database.repository.SeriesGenreRepository;
import com.moviefy.service.SeriesGenreService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class SeriesGenreServiceImpl implements SeriesGenreService {
    private final SeriesGenreRepository genreRepository;
    private final ApiConfig apiConfig;
    private final RestClient restClient;

    public SeriesGenreServiceImpl(SeriesGenreRepository genreRepository,
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
        List<SeriesGenre> genres = response
                .getGenres()
                .stream()
                .map(dto -> new SeriesGenre(dto.getName(), dto.getId()))
                .toList();

        this.genreRepository.saveAll(genres);
    }

    @Override
    public boolean isEmpty() {
        return this.genreRepository.count() == 0;
    }

    @Override
    public Set<SeriesGenre> getAllGenresByApiIds(Set<Long> genres) {
        Set<SeriesGenre> genresList = new HashSet<>();
        for (Long genre : genres) {
            Optional<SeriesGenre> optional = this.genreRepository.findByApiId(genre);
            optional.ifPresent(genresList::add);
        }
        return genresList;
    }

    @Override
    public List<SeriesGenre> getAllGenresByMovieId(Long id) {
        return this.genreRepository.findByTvSeriesId(id)
                .stream()
                .sorted(Comparator.comparing(SeriesGenre::getId))
                .toList();
    }

    private GenreResponseApiDTO getResponse() {
        String url = this.apiConfig.getUrl() + "/genre/tv/list?api_key=" + this.apiConfig.getKey();

        return this.restClient
                .get()
                .uri(url)
                .retrieve()
                .body(GenreResponseApiDTO.class);
    }
    public SeriesGenre getGenreByName(String genreType) {
        return this.genreRepository.findByName(genreType).orElse(null);
    }
}

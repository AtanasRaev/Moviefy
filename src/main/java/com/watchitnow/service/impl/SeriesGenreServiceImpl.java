package com.watchitnow.service.impl;

import com.watchitnow.config.ApiConfig;
import com.watchitnow.databse.model.dto.GenreResponseApiDTO;
import com.watchitnow.databse.model.entity.SeriesGenre;
import com.watchitnow.databse.repository.SeriesGenreRepository;
import com.watchitnow.databse.repository.TvSeriesRepository;
import com.watchitnow.service.SeriesGenreService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public List<SeriesGenre> getAllGenresByApiIds(List<Long> genres) {
        List<SeriesGenre> genresList = new ArrayList<>();
        for (Long genre : genres) {
            Optional<SeriesGenre> optional = this.genreRepository.findByApiId(genre);
            optional.ifPresent(genresList::add);
        }
        return genresList;
    }

    private GenreResponseApiDTO getResponse() {
        String url = this.apiConfig.getUrl() + "/genre/tv/list?api_key=" + this.apiConfig.getKey();

        return this.restClient
                .get()
                .uri(url)
                .retrieve()
                .body(GenreResponseApiDTO.class);
    }
}

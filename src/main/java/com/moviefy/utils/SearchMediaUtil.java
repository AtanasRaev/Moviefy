package com.moviefy.utils;

import com.moviefy.config.ApiConfig;
import com.moviefy.database.model.dto.apiDto.mediaDto.MediaApiDTO;
import com.moviefy.database.model.dto.apiDto.SearchResponseApiDTO;
import com.moviefy.database.model.dto.pageDto.SearchResultPageDTO;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.database.model.entity.media.tvSeries.SeasonTvSeries;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;
import com.moviefy.database.repository.media.MovieRepository;
import com.moviefy.database.repository.media.tvSeries.SeasonTvSeriesRepository;
import com.moviefy.database.repository.media.tvSeries.TvSeriesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SearchMediaUtil {

    private final ApiConfig apiConfig;
    private final RestClient restClient;
    private final MovieRepository movieRepository;
    private final TvSeriesRepository tvSeriesRepository;
    private final SeasonTvSeriesRepository seasonTvSeriesRepository;
    private final ModelMapper modelMapper;

    public SearchMediaUtil(ApiConfig apiConfig,
                           RestClient restClient,
                           MovieRepository movieRepository,
                           TvSeriesRepository tvSeriesRepository,
                           SeasonTvSeriesRepository seasonTvSeriesRepository,
                           ModelMapper modelMapper) {
        this.apiConfig = apiConfig;
        this.restClient = restClient;
        this.movieRepository = movieRepository;
        this.tvSeriesRepository = tvSeriesRepository;
        this.seasonTvSeriesRepository = seasonTvSeriesRepository;
        this.modelMapper = modelMapper;
    }

    public List<SearchResultPageDTO> search(String query) {
        SearchResponseApiDTO searchResponseApiDTO = this.searchQueryApi(query);

        if (searchResponseApiDTO == null || searchResponseApiDTO.getResults() == null || searchResponseApiDTO.getResults().isEmpty()) {
            return List.of();
        }

        Set<Long> tvApiIds = searchResponseApiDTO.getResults().stream()
                .filter(m -> "tv".equals(m.getMediaType()))
                .map(MediaApiDTO::getId)
                .collect(Collectors.toSet());

        Set<Long> movieApiIds = searchResponseApiDTO.getResults().stream()
                .filter(m -> "movie".equals(m.getMediaType()))
                .map(MediaApiDTO::getId)
                .collect(Collectors.toSet());

        List<TvSeries> seriesByApiId = tvApiIds.isEmpty()
                ? List.of()
                : this.tvSeriesRepository.findAllByApiIdIn(tvApiIds);

        List<Movie> moviesByApiId = movieApiIds.isEmpty()
                ? List.of()
                : this.movieRepository.findAllByApiIdIn(movieApiIds);

        if (seriesByApiId.isEmpty() && moviesByApiId.isEmpty()) {
            return List.of();
        }

        Map<Long, TvSeries> tvByApiId = seriesByApiId.stream()
                .collect(Collectors.toMap(TvSeries::getApiId, Function.identity()));

        Map<Long, Movie> movieByApiId = moviesByApiId.stream()
                .collect(Collectors.toMap(Movie::getApiId, Function.identity()));

        return searchResponseApiDTO.getResults().stream()
                .map(dto -> {
                    String mediaType = dto.getMediaType();

                    if ("tv".equals(mediaType)) {
                        TvSeries tvSeries = tvByApiId.get(dto.getId());
                        if (tvSeries == null) {
                            return null;
                        }

                        SearchResultPageDTO seriesDTO = this.modelMapper.map(tvSeries, SearchResultPageDTO.class);

                        if (tvSeries.getFirstAirDate() != null) {
                            seriesDTO.setYear(tvSeries.getFirstAirDate().getYear());
                        }

                        seriesDTO.setTitle(dto.getName());
                        seriesDTO.setType("series");

                        Set<SeasonTvSeries> seasons = this.seasonTvSeriesRepository.findAllByTvSeriesId(seriesDTO.getId());
                        seasons.stream()
                                .max(Comparator.comparing(SeasonTvSeries::getSeasonNumber))
                                .ifPresent(lastSeason -> {
                                    seriesDTO.setSeasonsCount(
                                            lastSeason.getSeasonNumber() > 411
                                                    ? lastSeason.getSeasonNumber()
                                                    : seasons.size()
                                    );
                                    seriesDTO.setEpisodesCount(lastSeason.getEpisodeCount());
                                });

                        return seriesDTO;
                    }

                    if ("movie".equals(mediaType)) {
                        Movie movie = movieByApiId.get(dto.getId());
                        if (movie == null) {
                            return null;
                        }

                        SearchResultPageDTO movieDTO = this.modelMapper.map(movie, SearchResultPageDTO.class);

                        movieDTO.setType("movie");

                        if (movie.getReleaseDate() != null) {
                            movieDTO.setYear(movie.getReleaseDate().getYear());
                        }

                        return movieDTO;
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }


    private SearchResponseApiDTO searchQueryApi(String query) {
        String url = String.format(this.apiConfig.getUrl()
                        + "/search/multi?api_key=%s&page=1&query=%s",
                this.apiConfig.getKey(), query);

        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(SearchResponseApiDTO.class);
        } catch (Exception e) {
            System.err.println("Error searching movies" + "- " + e.getMessage());
            return null;
        }

    }
}
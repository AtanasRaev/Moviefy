package com.moviefy.service.api.movie;

import com.moviefy.config.ApiConfig;
import com.moviefy.database.model.dto.apiDto.MovieApiByIdResponseDTO;
import com.moviefy.database.model.dto.apiDto.MovieResponseApiDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class TmdbMoviesEndpointServiceImpl implements TmdbMoviesEndpointService {
    private final ApiConfig apiConfig;
    private final RestClient restClient;

    private static final Logger logger = LoggerFactory.getLogger(TmdbMoviesEndpointServiceImpl.class);

    public TmdbMoviesEndpointServiceImpl(ApiConfig apiConfig,
                                         RestClient restClient) {
        this.apiConfig = apiConfig;
        this.restClient = restClient;
    }

    @Override
    public MovieResponseApiDTO getMoviesResponseByDateAndVoteCount(int page, int year) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(this.apiConfig.getUrl())
                .path("/discover/movie")
                .queryParam("primary_release_year", year)
                .queryParam("sort_by", "vote_count.desc")
                .queryParam("api_key", this.apiConfig.getKey())
                .queryParam("page", page)
                .build(true)
                .toUri();

        try {
            return this.restClient.get().uri(uri).retrieve().body(MovieResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching movies for year {} page {}. URL={}", year, page, uri, e);
            return null;
        }
    }

    @Override
    public MovieApiByIdResponseDTO getMovieResponseById(Long apiId) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(this.apiConfig.getUrl())
                .path("/movie/" + apiId)
                .queryParam("api_key", this.apiConfig.getKey())
                .queryParam("append_to_response", "credits")
                .build(true)
                .toUri();

        try {
            return this.restClient.get().uri(uri).retrieve().body(MovieApiByIdResponseDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching movie by ID {}. URL={}", apiId, uri, e);
            return null;
        }
    }

    @Override
    public MovieResponseApiDTO searchMoviesQueryApi(String query) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(this.apiConfig.getUrl())
                .path("/search/movie")
                .queryParam("api_key", this.apiConfig.getKey())
                .queryParam("page", 1)
                .queryParam("query", query)
                .build(true)
                .toUri();

        try {
            return this.restClient.get().uri(uri).retrieve().body(MovieResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error searching movies with query '{}'. URL={}", query, uri, e);
            return null;
        }
    }

    @Override
    public MovieResponseApiDTO getNewMoviesUTCTime(int page) {
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        LocalDate fromDate = todayUtc.minusDays(2);
        LocalDate toDate   = todayUtc.plusDays(2);

        URI uri = UriComponentsBuilder
                .fromHttpUrl(this.apiConfig.getUrl())
                .path("/discover/movie")
                .queryParam("primary_release_date.gte", fromDate)
                .queryParam("primary_release_date.lte", toDate)
                .queryParam("sort_by", "popularity.desc")
                .queryParam("api_key", this.apiConfig.getKey())
                .queryParam("page", page)
                .build(true)
                .toUri();

        try {
            return this.restClient.get().uri(uri).retrieve().body(MovieResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching today's discover movies ({} to {}). page={} URL={}",
                    fromDate, toDate, page, uri, e);
            return null;
        }
    }

    @Override
    public MovieResponseApiDTO getTrendingMovies(int page) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(this.apiConfig.getUrl())
                .path("/trending/movie/day")
                .queryParam("api_key", this.apiConfig.getKey())
                .queryParam("page", page)
                .build(true)
                .toUri();

        try {
            return this.restClient.get().uri(uri).retrieve().body(MovieResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching trending movies. page={} URL={}", page, uri, e);
            return null;
        }
    }
}

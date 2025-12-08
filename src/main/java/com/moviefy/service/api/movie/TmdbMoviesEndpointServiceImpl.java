package com.moviefy.service.api.movie;

import com.moviefy.config.ApiConfig;
import com.moviefy.database.model.dto.apiDto.MovieApiByIdResponseDTO;
import com.moviefy.database.model.dto.apiDto.MovieResponseApiDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
        String url = String.format(
                "%s/discover/movie?primary_release_year=%d&sort_by=vote_count.desc&api_key=%s&page=%d",
                apiConfig.getUrl(), year, apiConfig.getKey(), page
        );

        try {
            return restClient.get().uri(url).retrieve().body(MovieResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching movies for year {} page {}. URL={}", year, page, url, e);
            return null;
        }
    }

    @Override
    public MovieApiByIdResponseDTO getMovieResponseById(Long apiId) {
        String url = String.format(
                "%s/movie/%d?api_key=%s&append_to_response=credits",
                apiConfig.getUrl(), apiId, apiConfig.getKey()
        );

        try {
            return restClient.get().uri(url).retrieve().body(MovieApiByIdResponseDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching movie by ID {}. URL={}", apiId, url, e);
            return null;
        }
    }

    @Override
    public MovieResponseApiDTO searchMoviesQueryApi(String query) {
        String url = String.format(
                "%s/search/movie?api_key=%s&page=1&query=%s",
                apiConfig.getUrl(), apiConfig.getKey(), query
        );

        try {
            return restClient.get().uri(url).retrieve().body(MovieResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error searching movies with query '{}'. URL={}", query, url, e);
            return null;
        }
    }

    @Override
    public MovieResponseApiDTO getMoviesFromToday(int page) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        String fromDate = yesterday.format(DateTimeFormatter.ISO_DATE);
        String toDate   = today.format(DateTimeFormatter.ISO_DATE);

        String url = String.format(
                "%s/discover/movie?primary_release_date.gte=%s&primary_release_date.lte=%s&sort_by=popularity.desc&api_key=%s&page=%d",
                apiConfig.getUrl(), fromDate, toDate, apiConfig.getKey(), page
        );

        try {
            return restClient.get().uri(url).retrieve().body(MovieResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching today's discover movies ({} to {}). page={} URL={}",
                    fromDate, toDate, page, url, e);
            return null;
        }
    }

    @Override
    public MovieResponseApiDTO getTrendingMovies(int page) {
        String url = String.format(
                "%s/trending/movie/day?api_key=%s&page=%d",
                apiConfig.getUrl(), apiConfig.getKey(), page
        );

        try {
            return restClient.get().uri(url).retrieve().body(MovieResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching trending movies. page={} URL={}", page, url, e);
            return null;
        }
    }
}

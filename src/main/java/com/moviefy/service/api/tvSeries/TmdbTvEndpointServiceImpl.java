package com.moviefy.service.api.tvSeries;

import com.moviefy.config.ApiConfig;
import com.moviefy.database.model.dto.apiDto.EpisodesTvSeriesResponseDTO;
import com.moviefy.database.model.dto.apiDto.TvSeriesApiByIdResponseDTO;
import com.moviefy.database.model.dto.apiDto.TvSeriesResponseApiDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class TmdbTvEndpointServiceImpl implements TmdbTvEndpointService {
    private final ApiConfig apiConfig;
    private final RestClient restClient;

    private static final Logger logger = LoggerFactory.getLogger(TmdbTvEndpointServiceImpl.class);

    public TmdbTvEndpointServiceImpl(ApiConfig apiConfig, RestClient restClient) {
        this.apiConfig = apiConfig;
        this.restClient = restClient;
    }

    @Override
    public TvSeriesResponseApiDTO getTvSeriesResponseByDateAndVoteCount(int page, int year) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(this.apiConfig.getUrl())
                .path("/discover/tv")
                .queryParam("first_air_date.gte", year + "-01-01")
                .queryParam("first_air_date.lte", year + "-12-31")
                .queryParam("sort_by", "vote_count.desc")
                .queryParam("api_key", this.apiConfig.getKey())
                .queryParam("page", page)
                .build(true)
                .toUri();

        try {
            return this.restClient.get().uri(uri).retrieve().body(TvSeriesResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching TV series by date & vote count. year={}, page={}. URL={}",
                    year, page, uri, e);
            return null;
        }
    }

    @Override
    public TvSeriesApiByIdResponseDTO getTvSeriesResponseById(Long apiId) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(this.apiConfig.getUrl())
                .path("/tv/" + apiId)
                .queryParam("api_key", this.apiConfig.getKey())
                .queryParam("append_to_response", "credits,external_ids")
                .build(true)
                .toUri();

        try {
            return this.restClient.get().uri(uri).retrieve().body(TvSeriesApiByIdResponseDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching TV series by ID. apiId={}. URL={}", apiId, uri, e);
            return null;
        }
    }

    @Override
    public TvSeriesResponseApiDTO searchTvSeriesQueryApi(String query) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(this.apiConfig.getUrl())
                .path("/search/tv")
                .queryParam("api_key", this.apiConfig.getKey())
                .queryParam("page", 1)
                .queryParam("query", query)
                .build(true)
                .toUri();

        try {
            return this.restClient.get().uri(uri).retrieve().body(TvSeriesResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error searching TV series. query='{}'. URL={}", query, uri, e);
            return null;
        }
    }

    @Override
    public TvSeriesResponseApiDTO getNewTvSeriesUTCTime(int page) {
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        LocalDate fromDate = todayUtc.minusDays(2);
        LocalDate toDate   = todayUtc.plusDays(2);

        URI uri = UriComponentsBuilder
                .fromHttpUrl(this.apiConfig.getUrl())
                .path("/discover/tv")
                .queryParam("first_air_date.gte", fromDate)
                .queryParam("first_air_date.lte", toDate)
                .queryParam("sort_by", "popularity.desc")
                .queryParam("api_key", this.apiConfig.getKey())
                .queryParam("page", page)
                .build(true)
                .toUri();

        try {
            return this.restClient.get().uri(uri).retrieve().body(TvSeriesResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching discover TV series from {} to {}. page={}, URL={}",
                    fromDate, toDate, page, uri, e);
            return null;
        }
    }

    @Override
    public TvSeriesResponseApiDTO getTrendingSeries(int page) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(this.apiConfig.getUrl())
                .path("/trending/tv/day")
                .queryParam("api_key", this.apiConfig.getKey())
                .queryParam("page", page)
                .build(true)
                .toUri();

        try {
            return this.restClient.get().uri(uri).retrieve().body(TvSeriesResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching trending TV series. page={}, URL={}", page, uri, e);
            return null;
        }
    }

    @Override
    public EpisodesTvSeriesResponseDTO getEpisodesResponse(Long tvId, Integer season) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(this.apiConfig.getUrl())
                .path("/tv/" + tvId + "/season/" + season)
                .queryParam("api_key", this.apiConfig.getKey())
                .build(true)
                .toUri();

        try {
            return this.restClient.get().uri(uri).retrieve().body(EpisodesTvSeriesResponseDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching season for TV series. tvId={}, season={}, URL={}",
                    tvId, season, uri, e);
            return null;
        }
    }
}

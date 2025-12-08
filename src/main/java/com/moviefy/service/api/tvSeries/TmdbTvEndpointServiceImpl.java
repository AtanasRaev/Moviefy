package com.moviefy.service.api.tvSeries;

import com.moviefy.config.ApiConfig;
import com.moviefy.database.model.dto.apiDto.EpisodesTvSeriesResponseDTO;
import com.moviefy.database.model.dto.apiDto.TvSeriesApiByIdResponseDTO;
import com.moviefy.database.model.dto.apiDto.TvSeriesResponseApiDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
        String url = String.format(
                "%s/discover/tv?first_air_date.gte=%d-01-01&first_air_date.lte=%d-12-31&sort_by=vote_count.desc&api_key=%s&page=%d",
                apiConfig.getUrl(), year, year, apiConfig.getKey(), page
        );

        try {
            return restClient.get().uri(url).retrieve().body(TvSeriesResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching TV series by date & vote count. year={}, page={}. URL={}", year, page, url, e);
            return null;
        }
    }

    @Override
    public TvSeriesApiByIdResponseDTO getTvSeriesResponseById(Long apiId) {
        String url = String.format(
                "%s/tv/%d?api_key=%s&append_to_response=credits,external_ids",
                apiConfig.getUrl(), apiId, apiConfig.getKey()
        );

        try {
            return restClient.get().uri(url).retrieve().body(TvSeriesApiByIdResponseDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching TV series by ID. apiId={}. URL={}", apiId, url, e);
            return null;
        }
    }

    @Override
    public TvSeriesResponseApiDTO searchTvSeriesQueryApi(String query) {
        String url = String.format(
                "%s/search/tv?api_key=%s&page=1&query=%s",
                apiConfig.getUrl(), apiConfig.getKey(), query
        );

        try {
            return restClient.get().uri(url).retrieve().body(TvSeriesResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error searching TV series. query='{}'. URL={}", query, url, e);
            return null;
        }
    }

    @Override
    public TvSeriesResponseApiDTO getSeriesFromToday(int page) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        String fromDate = yesterday.format(DateTimeFormatter.ISO_DATE);
        String toDate = today.format(DateTimeFormatter.ISO_DATE);

        String url = String.format(
                "%s/discover/tv?first_air_date.gte=%s&first_air_date.lte=%s&sort_by=popularity.desc&api_key=%s&page=%d",
                apiConfig.getUrl(), fromDate, toDate, apiConfig.getKey(), page
        );

        try {
            return restClient.get().uri(url).retrieve().body(TvSeriesResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching discover TV series from {} to {}. page={}, URL={}", fromDate, toDate, page, url, e);
            return null;
        }
    }

    @Override
    public TvSeriesResponseApiDTO getTrendingSeries(int page) {
        String url = String.format(
                "%s/trending/tv/day?api_key=%s&page=%d",
                apiConfig.getUrl(), apiConfig.getKey(), page
        );

        try {
            return restClient.get().uri(url).retrieve().body(TvSeriesResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching trending TV series. page={}, URL={}", page, url, e);
            return null;
        }
    }

    @Override
    public EpisodesTvSeriesResponseDTO getEpisodesResponse(Long tvId, Integer season) {
        String url = String.format(
                "%s/tv/%d/season/%d?api_key=%s",
                apiConfig.getUrl(), tvId, season, apiConfig.getKey()
        );

        try {
            return restClient.get().uri(url).retrieve().body(EpisodesTvSeriesResponseDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching season for TV series. tvId={}, season={}, URL={}", tvId, season, url, e);
            return null;
        }
    }
}

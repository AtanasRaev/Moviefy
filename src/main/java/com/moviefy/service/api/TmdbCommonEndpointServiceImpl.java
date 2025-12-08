package com.moviefy.service.api;

import com.moviefy.config.ApiConfig;
import com.moviefy.database.model.dto.apiDto.ReviewResponseApiDTO;
import com.moviefy.database.model.dto.apiDto.TrailerResponseApiDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class TmdbCommonEndpointServiceImpl implements TmdbCommonEndpointService {
    private final ApiConfig apiConfig;
    private final RestClient restClient;

    private static final Logger logger = LoggerFactory.getLogger(TmdbCommonEndpointServiceImpl.class);

    public TmdbCommonEndpointServiceImpl(ApiConfig apiConfig,
                                         RestClient restClient) {
        this.apiConfig = apiConfig;
        this.restClient = restClient;
    }

    @Override
    public TrailerResponseApiDTO getTrailerResponseById(Long apiId, String type) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(this.apiConfig.getUrl())
                .path("/" + type + "/" + apiId + "/videos")
                .queryParam("api_key", this.apiConfig.getKey())
                .build(true)
                .toUri();

        try {
            return this.restClient.get().uri(uri).retrieve().body(TrailerResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching trailer. type={}, apiId={}, URL={}", type, apiId, uri, e);
            return null;
        }
    }

    @Override
    public ReviewResponseApiDTO getReviewsResponseApi(String mediaType, long apiId, int page) {
        String type = mediaType.equalsIgnoreCase("series") ? "tv" : "movie";

        URI uri = UriComponentsBuilder
                .fromHttpUrl(this.apiConfig.getUrl())
                .path("/" + type + "/" + apiId + "/reviews")
                .queryParam("api_key", this.apiConfig.getKey())
                .queryParam("page", page)
                .build(true)
                .toUri();

        try {
            return this.restClient.get().uri(uri).retrieve().body(ReviewResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching reviews. mediaType={}, apiId={}, page={}, URL={}",
                    mediaType, apiId, page, uri, e);
            return null;
        }
    }
}

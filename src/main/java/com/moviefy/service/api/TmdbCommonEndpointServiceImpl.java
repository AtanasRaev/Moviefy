package com.moviefy.service.api;

import com.moviefy.config.ApiConfig;
import com.moviefy.database.model.dto.apiDto.ReviewResponseApiDTO;
import com.moviefy.database.model.dto.apiDto.TrailerResponseApiDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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
        String url = String.format(
                "%s/%s/%d/videos?api_key=%s",
                apiConfig.getUrl(), type, apiId, apiConfig.getKey()
        );

        try {
            return restClient.get().uri(url).retrieve().body(TrailerResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching trailer. type={}, apiId={}, URL={}", type, apiId, url, e);
            return null;
        }
    }

    @Override
    public ReviewResponseApiDTO getReviewsResponseApi(String mediaType, long apiId, int page) {
        String type = mediaType.equalsIgnoreCase("series") ? "tv" : "movie";

        String url = String.format(
                "%s/%s/%d/reviews?api_key=%s&page=%d",
                apiConfig.getUrl(), type, apiId, apiConfig.getKey(), page
        );

        try {
            return restClient.get().uri(url).retrieve().body(ReviewResponseApiDTO.class);
        } catch (Exception e) {
            logger.error("Error fetching reviews. mediaType={}, apiId={}, page={}, URL={}",
                    mediaType, apiId, page, url, e);
            return null;
        }
    }
}

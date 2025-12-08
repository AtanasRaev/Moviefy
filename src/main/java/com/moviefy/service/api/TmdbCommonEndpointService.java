package com.moviefy.service.api;

import com.moviefy.database.model.dto.apiDto.ReviewResponseApiDTO;
import com.moviefy.database.model.dto.apiDto.TrailerResponseApiDTO;

public interface TmdbCommonEndpointService {
    TrailerResponseApiDTO getTrailerResponseById(Long apiId, String type);

    ReviewResponseApiDTO getReviewsResponseApi(String mediaType, long apiId, int page);
}

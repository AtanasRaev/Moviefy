package com.moviefy.service.api;

import com.moviefy.database.model.dto.apiDto.reviewDto.ReviewResponseApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.TrailerResponseApiDTO;

public interface TmdbCommonEndpointService {
    TrailerResponseApiDTO getTrailerResponseById(Long apiId, String type);

    ReviewResponseApiDTO getReviewsResponseApi(String mediaType, long apiId, int page);
}

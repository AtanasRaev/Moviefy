package com.moviefy.utils;

import com.moviefy.database.model.dto.apiDto.TrailerApiDTO;
import com.moviefy.database.model.dto.apiDto.TrailerResponseApiDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.List;

@Component
public class TrailerMappingUtil {
    private final RestClient restClient;

    public TrailerMappingUtil(RestClient restClient) {
        this.restClient = restClient;
    }

    public TrailerApiDTO getTrailer(List<TrailerApiDTO> trailers) {
        List<TrailerApiDTO> filter = trailers.stream().filter(t -> "Trailer".equals(t.getType())).toList();

        if (!filter.isEmpty()) {
            trailers = filter;
        }

        TrailerApiDTO selectedTrailer;

        if (trailers.size() == 1) {
            selectedTrailer = trailers.get(0);
        } else {
            selectedTrailer = trailers.stream()
                    .filter(trailer -> (trailer.getName().contains("Final")))
                    .findFirst()
                    .orElse(null);

            if (selectedTrailer == null) {
                selectedTrailer = trailers.stream()
                        .filter(trailer -> (trailer.getName().contains("Official")))
                        .findFirst()
                        .orElse(null);
            }

            if (selectedTrailer == null) {
                selectedTrailer = trailers.stream()
                        .min(Comparator.comparing(TrailerApiDTO::getPublishedAt))
                        .orElse(null);
            }
        }

        return selectedTrailer;
    }

    public TrailerResponseApiDTO getTrailerResponseById(Long apiId, String apiUrl, String apiKey, String type) {
        String url = String.format(apiUrl + "/%s/%d/videos?api_key=" + apiKey, type, apiId);
        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(TrailerResponseApiDTO.class);
        } catch (Exception e) {
            System.err.println("Error fetching movie with ID: " + apiId + " - " + e.getMessage());
            return null;
        }
    }
}

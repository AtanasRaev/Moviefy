package com.moviefy.utils.mappers;

import com.moviefy.database.model.dto.apiDto.mediaDto.MediaApiByIdResponseDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.TrailerApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.TrailerResponseApiDTO;
import com.moviefy.database.model.entity.media.Media;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

public abstract class MediaMapper {
    protected void mapCommonFields(Media media, MediaApiByIdResponseDTO dto, TrailerResponseApiDTO responseTrailer) {
        BigDecimal popularity = BigDecimal.valueOf(dto.getPopularity()).setScale(1, RoundingMode.HALF_UP);
        BigDecimal voteAverage = BigDecimal.valueOf(dto.getVoteAverage()).setScale(1, RoundingMode.HALF_UP);

        media.setApiId(dto.getId());
        media.setOverview(dto.getOverview());
        media.setPosterPath(dto.getPosterPath());
        media.setBackdropPath(dto.getBackdropPath());
        media.setVoteCount(dto.getVoteCount());
        media.setPopularity(popularity.doubleValue());
        media.setVoteAverage(voteAverage.doubleValue());
        media.setRefreshedAt(null);
        media.setFavouriteCount(0);

        List<TrailerApiDTO> trailers = responseTrailer.getResults();
        if (!trailers.isEmpty()) {
            TrailerApiDTO selectedTrailer = this.getTrailer(trailers);
            if (selectedTrailer != null) {
                media.setTrailer(selectedTrailer.getKey());
            }
        }
    }

    private TrailerApiDTO getTrailer(List<TrailerApiDTO> trailers) {
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
}

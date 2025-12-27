package com.moviefy.service.scheduling.evaluation;

import com.moviefy.config.FetchMediaConfig;
import com.moviefy.database.model.dto.apiDto.mediaDto.tvSeriesDto.TvSeriesApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.tvSeriesDto.TvSeriesResponseApiDTO;
import com.moviefy.database.repository.media.tvSeries.TvSeriesRepository;
import com.moviefy.service.api.tvSeries.TmdbTvEndpointService;
import com.moviefy.service.scheduling.IngestEnum;
import com.moviefy.service.scheduling.persistence.TvSeriesPersistenceWorker;
import com.moviefy.utils.MediaValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.moviefy.utils.Ansi.*;

@Service
public class TvSeriesEvaluationOrchestrator {
    private final TvSeriesRepository tvSeriesRepository;
    private final TmdbTvEndpointService tmdbTvEndpointService;
    private final TvSeriesPersistenceWorker tvSeriesPersistenceWorker;

    private final Logger logger = LoggerFactory.getLogger(TvSeriesEvaluationOrchestrator.class);

    public TvSeriesEvaluationOrchestrator(TvSeriesRepository tvSeriesRepository,
                                          TmdbTvEndpointService tmdbTvEndpointService,
                                          TvSeriesPersistenceWorker tvSeriesPersistenceWorker) {
        this.tvSeriesRepository = tvSeriesRepository;
        this.tmdbTvEndpointService = tmdbTvEndpointService;
        this.tvSeriesPersistenceWorker = tvSeriesPersistenceWorker;
    }

    @Async
    public CompletableFuture<List<Long>> evaluateTvSeries() {
        logger.info(BLUE + "📺 Starting TV SERIES EVALUATION job (thread={})" + RESET, Thread.currentThread().getName());
        LocalDate today = LocalDate.now();

        List<Long> insertedToday = new ArrayList<>();

        int totalPages = (int) Math.ceil((double) FetchMediaConfig.MAX_MEDIA_PER_YEAR / FetchMediaConfig.API_MEDIA_PER_PAGE) + 10;
        Set<Long> apiIds = new LinkedHashSet<>();

        for (int page = 1; page <= totalPages; page++) {
            TvSeriesResponseApiDTO response = this.tmdbTvEndpointService.getTvSeriesResponseByDateAndVoteCount(page, today.getYear());

            if (response == null || response.getResults() == null) {
                logger.warn(YELLOW + "No results returned for page {}" + RESET, page);
                break;
            }

            if (page > response.getTotalPages()) {
                break;
            }

            for (TvSeriesApiDTO dto : response.getResults()) {
                if (MediaValidationUtil.isInvalid(dto)) {
                    logger.warn(YELLOW + "Invalid TV series: {}" + RESET, dto.getId());
                    continue;
                }

                apiIds.add(dto.getId());
            }

            if (apiIds.size() >= FetchMediaConfig.MAX_MEDIA_PER_YEAR) {
                break;
            }
        }

        Set<Long> allApiIdsByApiIdIn = this.tvSeriesRepository.findAllApiIdsByApiIdIn(apiIds);
        apiIds.removeAll(allApiIdsByApiIdIn);

        if (apiIds.isEmpty()) {
            logger.info(BLUE + "No new TV series to evaluate for year {}" + RESET, today.getYear());
            return CompletableFuture.completedFuture(List.of());
        }

        logger.info(BLUE + "Evaluating {} potential new TV series for year {}…" + RESET, apiIds.size(), today.getYear());

        for (Long apiId : apiIds) {
            try {
                IngestEnum result = this.tvSeriesPersistenceWorker.persistSeriesIfEligible(apiId);

                if (result == IngestEnum.INSERTED) {
                    insertedToday.add(apiId);
                } else if (result == IngestEnum.STOP_EVALUATION) {
                    return CompletableFuture.completedFuture(insertedToday);
                }
            } catch (Exception ex) {
                logger.error(RED + "Failed to evaluate TV series apiId={}" + RESET, apiId, ex);
            }
        }
        logger.info(BLUE + "TV series evaluation finished: {} series inserted/replaced." + RESET, insertedToday.size());
        return CompletableFuture.completedFuture(insertedToday);
    }
}

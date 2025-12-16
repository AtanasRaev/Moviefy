package com.moviefy.service.scheduling.refresh.tvSeries;

import com.moviefy.database.model.dto.apiDto.mediaDto.tvSeriesDto.TvSeriesApiByIdResponseDTO;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;
import com.moviefy.database.repository.media.tvSeries.TvSeriesRepository;
import com.moviefy.service.media.tvSeries.seasons.SeasonsService;
import com.moviefy.utils.mappers.TvSeriesRefreshMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.moviefy.utils.Ansi.*;

@Service
public class TvSeriesRefreshItemService {
    private final TvSeriesRepository tvSeriesRepository;
    private final SeasonsService seasonsService;

    private static final Logger logger = LoggerFactory.getLogger(TvSeriesRefreshItemService.class);

    public TvSeriesRefreshItemService(TvSeriesRepository tvSeriesRepository,
                                      SeasonsService seasonsService) {
        this.tvSeriesRepository = tvSeriesRepository;
        this.seasonsService = seasonsService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean refreshOneTvSeries(Long apiId,
                                      TvSeriesApiByIdResponseDTO dto,
                                      LocalDateTime now) {

        logger.debug(CYAN + "📺 Refreshing TV series apiId={} (tx=REQUIRES_NEW)" + RESET, apiId);

        Optional<TvSeries> opt = this.tvSeriesRepository.findByApiId(apiId);
        if (opt.isEmpty()) {
            logger.warn(YELLOW + "Skip TV refresh: tvApiId={} not found in database" + RESET, apiId);
            return false;
        }

        TvSeries series = opt.get();

        boolean updated;
        try {
            updated = TvSeriesRefreshMapper.mapTvSeries(series, dto, now, this.seasonsService);
        } catch (Exception ex) {
            logger.error(RED + "❌ Failed to map refresh fields for tvApiId={}" + RESET, apiId, ex);
            return false;
        }

        if (!updated) {
            logger.debug(YELLOW + "No changes detected for tvApiId={} — already up to date" + RESET, apiId);
            return false;
        }

        try {
            this.tvSeriesRepository.save(series);
            logger.info(GREEN + "✔ Updated TV series apiId={} ({})" + RESET,
                    apiId,
                    series.getName());
        } catch (Exception ex) {
            logger.error(RED + "❌ Failed to save refreshed TV series apiId={}" + RESET, apiId, ex);
            return false;
        }

        return true;
    }
}

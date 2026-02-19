package com.moviefy.service.scheduling.refresh.tvSeries;

import com.moviefy.database.model.dto.apiDto.mediaDto.tvSeriesDto.TvSeriesApiByIdResponseDTO;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;
import com.moviefy.database.repository.credit.cast.CastTvSeriesRepository;
import com.moviefy.database.repository.credit.crew.CrewTvSeriesRepository;
import com.moviefy.database.repository.media.tvSeries.TvSeriesRepository;
import com.moviefy.service.media.tvSeries.seasons.SeasonsService;
import com.moviefy.service.scheduling.MediaEventPublisher;
import com.moviefy.utils.mappers.TvSeriesRefreshMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static com.moviefy.utils.Ansi.*;

@Service
public class TvSeriesRefreshWorker {
    private final TvSeriesRepository tvSeriesRepository;
    private final CastTvSeriesRepository castTvSeriesRepository;
    private final CrewTvSeriesRepository crewTvSeriesRepository;
    private final SeasonsService seasonsService;
    private final MediaEventPublisher mediaEventPublisher;

    private static final Logger logger = LoggerFactory.getLogger(TvSeriesRefreshWorker.class);

    public TvSeriesRefreshWorker(TvSeriesRepository tvSeriesRepository,
                                 CastTvSeriesRepository castTvSeriesRepository,
                                 CrewTvSeriesRepository crewTvSeriesRepository,
                                 SeasonsService seasonsService,
                                 MediaEventPublisher mediaEventPublisher) {
        this.tvSeriesRepository = tvSeriesRepository;
        this.castTvSeriesRepository = castTvSeriesRepository;
        this.crewTvSeriesRepository = crewTvSeriesRepository;
        this.seasonsService = seasonsService;
        this.mediaEventPublisher = mediaEventPublisher;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean refreshOneTvSeries(Long apiId,
                                      TvSeriesApiByIdResponseDTO dto,
                                      LocalDateTime now) {

        logger.debug(BLUE + "📺 Refreshing TV series apiId={} (tx=REQUIRES_NEW)" + RESET, apiId);

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
        } catch (Exception ex) {
            logger.error(RED + "❌ Failed to save refreshed TV series apiId={}" + RESET, apiId, ex);
            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean deleteMissingTvSeriesByApiId(Long apiId) {
        Optional<TvSeries> opt = this.tvSeriesRepository.findByApiId(apiId);
        if (opt.isEmpty()) {
            logger.warn(YELLOW + "Skip delete: tvApiId={} not found in database" + RESET, apiId);
            return false;
        }

        TvSeries tv = opt.get();

        try {
            Set<Long> castIds = this.castTvSeriesRepository.findCastIdsByTvSeriesId(tv.getId());
            this.castTvSeriesRepository.deleteByTvSeriesId(tv.getId());
            if (!castIds.isEmpty()) {
                this.mediaEventPublisher.publishCastByTvSeriesChangedEvent(castIds);
                this.mediaEventPublisher.publishCastByMediaChangedEvent(castIds);
            }

            Set<Long> crewIds = this.crewTvSeriesRepository.findCrewIdsByTvSeriesId(tv.getId());
            this.crewTvSeriesRepository.deleteByTvSeriesId(tv.getId());
            if (!crewIds.isEmpty()) {
                this.mediaEventPublisher.publishCrewByTvSeriesChangedEvent(crewIds);
                this.mediaEventPublisher.publishCrewByMediaChangedEvent(crewIds);
            }

            this.tvSeriesRepository.deleteFavoritesByTvSeriesId(tv.getId());

            if (tv.getGenres() != null) {
                tv.getGenres().clear();
            }
            if (tv.getProductionCompanies() != null) {
                tv.getProductionCompanies().clear();
            }
            if (tv.getSeasons() != null) {
                tv.getSeasons().forEach(season -> season.getEpisodes().clear());
                tv.getSeasons().clear();
            }

            this.tvSeriesRepository.delete(tv);
            return true;
        } catch (Exception ex) {
            logger.error(RED + "Failed to delete missing TV series apiId={}" + RESET, apiId, ex);
            return false;
        }
    }
}

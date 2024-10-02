package com.watchitnow.service.impl;

import com.watchitnow.config.ApiConfig;
import com.watchitnow.database.model.dto.SeasonDTO;
import com.watchitnow.database.model.dto.SeasonTvSeriesResponseApiDTO;
import com.watchitnow.database.model.dto.TvSeriesApiDTO;
import com.watchitnow.database.model.dto.TvSeriesResponseApiDTO;
import com.watchitnow.database.model.entity.SeasonTvSeries;
import com.watchitnow.database.model.entity.TvSeries;
import com.watchitnow.database.repository.SeasonTvSeriesRepository;
import com.watchitnow.database.repository.TvSeriesRepository;
import com.watchitnow.service.SeriesGenreService;
import com.watchitnow.service.TvSeriesService;
import com.watchitnow.utils.DatePaginationUtil;
import com.watchitnow.utils.DateRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TvSeriesServiceImpl implements TvSeriesService {
    private final TvSeriesRepository tvSeriesRepository;
    private final SeriesGenreService seriesGenreService;
    private final SeasonTvSeriesRepository seasonTvSeriesRepository;
    private final ApiConfig apiConfig;
    private final RestClient restClient;
    private static final Logger logger = LoggerFactory.getLogger(MovieServiceImpl.class);

    public TvSeriesServiceImpl(TvSeriesRepository tvSeriesRepository,
                               SeriesGenreService seriesGenreService,
                               SeasonTvSeriesRepository seasonTvSeriesRepository,
                               ApiConfig apiConfig,
                               RestClient restClient) {
        this.tvSeriesRepository = tvSeriesRepository;
        this.seriesGenreService = seriesGenreService;
        this.seasonTvSeriesRepository = seasonTvSeriesRepository;
        this.apiConfig = apiConfig;
        this.restClient = restClient;
    }

//    @Scheduled(fixedDelay = 5000)
    private void fetchSeries() {
        logger.info("Starting to fetch tv series...");
        int year = LocalDate.now().getYear();
        LocalDate startDate = LocalDate.of(year, 12, 1);
        int page = 1;

        if (!isEmpty()) {
            List<TvSeries> oldestTvSeries = this.tvSeriesRepository.findOldestTvSeries();
            if (!oldestTvSeries.isEmpty()) {
                TvSeries oldestTV = oldestTvSeries.get(0);
                year = oldestTV.getFirstAirDate().getYear();
                startDate = LocalDate.of(year, oldestTV.getFirstAirDate().getMonthValue(), oldestTV.getFirstAirDate().getDayOfMonth());
                long moviesByYearAndMonth = this.tvSeriesRepository.countTvSeriesInDateRange(oldestTV.getFirstAirDate().getYear(), oldestTV.getFirstAirDate().getMonthValue());
                if (moviesByYearAndMonth > 20) {
                    page = (int) ((moviesByYearAndMonth / 20) + 1);
                }
            }
        }

        int totalPages;
        LocalDate endDate = LocalDate.of(year, startDate.getMonthValue(), startDate.lengthOfMonth());

        int savedSeriesCount = 0;

        for (int i = 0; i < 40; i++) {
            logger.info("Fetching page {} of date range {} to {}", page, startDate, endDate);

            TvSeriesResponseApiDTO response = getTvSeriesResponse(page, startDate, endDate);
            totalPages = response.getTotalPages();

            for (TvSeriesApiDTO dto : response.getResults()) {
                if (this.tvSeriesRepository.findByApiId(dto.getId()).isEmpty()) {

                    SeasonTvSeriesResponseApiDTO seasonsResponse = getSeasonsResponse(dto.getId());
                    TvSeries tvSeries = new TvSeries();

                    tvSeries.setGenres(this.seriesGenreService.getAllGenresByApiIds(dto.getGenres()));
                    tvSeries.setApiId(dto.getId());
                    tvSeries.setName(dto.getName());
                    tvSeries.setOverview(dto.getOverview());
                    tvSeries.setPopularity(dto.getPopularity());
                    tvSeries.setPosterPath(dto.getPosterPath());
                    tvSeries.setFirstAirDate(dto.getFirstAirDate());

                    List<SeasonTvSeries> seasons = new ArrayList<>();

                    for (SeasonDTO seasonDTO : seasonsResponse.getSeasons()) {
                        if (this.seasonTvSeriesRepository.findByApiId(seasonDTO.getId()).isEmpty()) {
                            SeasonTvSeries season = new SeasonTvSeries();
                            season.setApiId(seasonDTO.getId());
                            season.setSeasonNumber(seasonDTO.getSeasonNumber());
                            season.setAirDate(seasonDTO.getAirDate());
                            season.setEpisodeCount(seasonDTO.getEpisodeCount());
                            season.setTvSeries(tvSeries);
                            seasons.add(season);
                        }
                    }

                    tvSeries.setSeasons(seasons);
                    this.tvSeriesRepository.save(tvSeries);
                    this.seasonTvSeriesRepository.saveAll(seasons);
                    savedSeriesCount++;
                    logger.info("Saved tv series: {}", tvSeries.getName());
                }
            }
            DateRange result = DatePaginationUtil.updatePageAndDate(page, totalPages, i, savedSeriesCount, startDate, endDate, year);
            page = result.getPage();
            startDate = result.getStartDate();
            endDate = result.getEndDate();
            year = result.getYear();
        }
        logger.info("Finished fetching movies.");
    }

    private boolean isEmpty() {
        return this.tvSeriesRepository.count() == 0;
    }

    private TvSeriesResponseApiDTO getTvSeriesResponse(int page, LocalDate startDate, LocalDate endDate) {
        String url = String.format(this.apiConfig.getUrl()
                        + "/discover/tv?page=%d&first_air_date.gte=%s&first_air_date.lte=%s&api_key="
                        + this.apiConfig.getKey()
                , page, startDate, endDate);
        return this.restClient
                .get()
                .uri(url)
                .retrieve()
                .body(TvSeriesResponseApiDTO.class);
    }

    private SeasonTvSeriesResponseApiDTO getSeasonsResponse(long id) {
        String url = String.format(this.apiConfig.getUrl() + "/tv/%d?api_key=%s", id, this.apiConfig.getKey());

        return this.restClient
                .get()
                .uri(url)
                .retrieve()
                .body(SeasonTvSeriesResponseApiDTO.class);
    }
}

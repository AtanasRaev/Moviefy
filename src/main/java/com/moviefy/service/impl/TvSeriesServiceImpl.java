package com.moviefy.service.impl;

import com.moviefy.config.ApiConfig;
import com.moviefy.database.model.dto.apiDto.*;
import com.moviefy.database.model.dto.databaseDto.EpisodeDTO;
import com.moviefy.database.model.dto.databaseDto.SeasonDTO;
import com.moviefy.database.model.dto.detailsDto.SeasonTvSeriesDTO;
import com.moviefy.database.model.dto.detailsDto.TvSeriesDetailsDTO;
import com.moviefy.database.model.dto.pageDto.GenrePageDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesPageDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesPageWithGenreDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesTrendingPageDTO;
import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.credit.cast.Cast;
import com.moviefy.database.model.entity.credit.cast.CastTvSeries;
import com.moviefy.database.model.entity.credit.crew.Crew;
import com.moviefy.database.model.entity.credit.crew.CrewTvSeries;
import com.moviefy.database.model.entity.genre.SeriesGenre;
import com.moviefy.database.model.entity.media.EpisodeTvSeries;
import com.moviefy.database.model.entity.media.SeasonTvSeries;
import com.moviefy.database.model.entity.media.TvSeries;
import com.moviefy.database.repository.*;
import com.moviefy.service.*;
import com.moviefy.utils.TrailerMappingUtil;
import com.moviefy.utils.TvSeriesMapper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TvSeriesServiceImpl implements TvSeriesService {
    private final TvSeriesRepository tvSeriesRepository;
    private final CrewTvSeriesRepository crewTvSeriesRepository;
    private final CastTvSeriesRepository castTvSeriesRepository;
    private final SeriesGenreService seriesGenreService;
    private final CastService castService;
    private final CrewService crewService;
    private final SeasonTvSeriesRepository seasonTvSeriesRepository;
    private final EpisodeTvSeriesRepository episodeTvSeriesRepository;
    private final ProductionCompanyService productionCompanyService;
    private final ApiConfig apiConfig;
    private final RestClient restClient;
    private final TrailerMappingUtil trailerMappingUtil;
    private final ModelMapper modelMapper;
    private final TvSeriesMapper tvSeriesMapper;
    private static final Logger logger = LoggerFactory.getLogger(TvSeriesServiceImpl.class);
    private static final int START_YEAR = 1970;
    private static final int MAX_TV_SERIES_PER_YEAR = 1200;
    private static final double API_TV_SERIES_PER_PAGE = 20.0;

    public TvSeriesServiceImpl(TvSeriesRepository tvSeriesRepository,
                               CrewTvSeriesRepository crewTvSeriesRepository,
                               CastTvSeriesRepository castTvSeriesRepository,
                               SeriesGenreService seriesGenreService,
                               CastService castService,
                               CrewService crewService,
                               SeasonTvSeriesRepository seasonTvSeriesRepository,
                               EpisodeTvSeriesRepository episodeTvSeriesRepository,
                               ProductionCompanyService productionCompanyService,
                               ApiConfig apiConfig,
                               RestClient restClient,
                               TrailerMappingUtil trailerMappingUtil,
                               ModelMapper modelMapper,
                               TvSeriesMapper tvSeriesMapper) {
        this.tvSeriesRepository = tvSeriesRepository;
        this.crewTvSeriesRepository = crewTvSeriesRepository;
        this.castTvSeriesRepository = castTvSeriesRepository;
        this.seriesGenreService = seriesGenreService;
        this.castService = castService;
        this.crewService = crewService;
        this.seasonTvSeriesRepository = seasonTvSeriesRepository;
        this.episodeTvSeriesRepository = episodeTvSeriesRepository;
        this.productionCompanyService = productionCompanyService;
        this.apiConfig = apiConfig;
        this.restClient = restClient;
        this.trailerMappingUtil = trailerMappingUtil;
        this.modelMapper = modelMapper;
        this.tvSeriesMapper = tvSeriesMapper;
    }

    @Override
    public Page<TvSeriesPageDTO> getTvSeriesFromCurrentMonth(Pageable pageable) {
        return this.tvSeriesRepository.findByFirstAirDate(
                getStartOfCurrentMonth(),
                pageable
        ).map(this::mapTvSeriesPageDTO);
    }

    @Override
    public TvSeriesDetailsDTO getTvSeriesDetailsById(Long id) {
        return this.tvSeriesRepository.findTvSeriesById(id)
                .map(this::mapToTvSeriesDetailsDTO)
                .orElse(null);
    }

    @Override
    public Set<TvSeriesPageDTO> getTvSeriesByGenre(String genreType) {
        return this.tvSeriesRepository.findByGenreName(genreType)
                .stream()
                .map(tvSeries -> modelMapper.map(tvSeries, TvSeriesPageDTO.class))
                .collect(Collectors.toSet());
    }

    @Override
    public Page<TvSeriesTrendingPageDTO> getTrendingTvSeries(Pageable pageable) {
        LocalDate today = LocalDate.now();
        List<SeasonTvSeries> allByYearRange = this.seasonTvSeriesRepository.findAllByYearRange(today.minusYears(1).getYear(), today.getYear());

        List<Long> ids = allByYearRange
                .stream()
                .mapToLong(s -> s.getTvSeries().getId())
                .boxed()
                .toList();

        return this.tvSeriesRepository.findAllBySeasonsIds(ids, pageable)
                .map(tvSeries -> {
                    TvSeriesTrendingPageDTO map = this.modelMapper.map(tvSeries, TvSeriesTrendingPageDTO.class);
                    mapAllGenresToPageDTO(map);
                    mapSeasonsToPageDTO(new HashSet<>(this.seasonTvSeriesRepository.findAllByTvSeriesId(map.getId())), map);
                    return map;
                });
    }

    @Override
    public Page<TvSeriesPageWithGenreDTO> getPopularTvSeries(Pageable pageable) {
        return this.tvSeriesRepository.findAllSortedByVoteCount(pageable)
                .map(tvSeries -> {
                    TvSeriesPageWithGenreDTO map = this.modelMapper.map(tvSeries, TvSeriesPageWithGenreDTO.class);
                    mapOneGenreToPageDTO(map);
                    mapSeasonsToPageDTO(new HashSet<>(this.seasonTvSeriesRepository.findAllByTvSeriesId(map.getId())), map);
                    return map;
                });
    }

    @Override
    public boolean isEmpty() {
        return this.tvSeriesRepository.count() == 0;
    }

    @Override
    public List<TvSeriesTrendingPageDTO> getHomeSeriesDTO(List<String> input) {
        return this.tvSeriesRepository.findAllByNames(input)
                .stream()
                .map(tvSeries -> {
                    TvSeriesTrendingPageDTO map = this.modelMapper.map(tvSeries, TvSeriesTrendingPageDTO.class);
                    mapAllGenresToPageDTO(map);
                    mapSeasonsToPageDTO(new HashSet<>(this.seasonTvSeriesRepository.findAllByTvSeriesId(map.getId())), map);
                    return map;
                })
                .toList();
    }

    @Override
    public List<EpisodeDTO> getEpisodesFromSeason(Long seasonId) {
        return this.episodeTvSeriesRepository.findAllBySeasonId(seasonId)
                .stream()
                .map(episode -> this.modelMapper.map(episode, EpisodeDTO.class))
                .sorted(Comparator.comparing(EpisodeDTO::getEpisodeNumber))
                .toList();
    }

    @Override
    public Integer getSeasonNumberById(Long seasonId) {
        return this.seasonTvSeriesRepository.findById(seasonId)
                .map(SeasonTvSeries::getSeasonNumber)
                .orElse(null);
    }

    @Override
    public Page<TvSeriesPageWithGenreDTO> searchTvSeries(String query, Pageable pageable) {
        return this.tvSeriesRepository.searchByName(query, pageable)
                .map(tvSeries -> {
                    TvSeriesPageWithGenreDTO map = this.modelMapper.map(tvSeries, TvSeriesPageWithGenreDTO.class);
                    mapOneGenreToPageDTO(map);
                    mapSeasonsToPageDTO(new HashSet<>(this.seasonTvSeriesRepository.findAllByTvSeriesId(map.getId())), map);
                    return map;
                });
    }

    private TvSeriesPageDTO mapTvSeriesPageDTO(TvSeries tvSeries) {
        TvSeriesPageDTO map = modelMapper.map(tvSeries, TvSeriesPageDTO.class);
        map.setYear(tvSeries.getFirstAirDate().getYear());
        List<SeasonTvSeries> seasons = this.seasonTvSeriesRepository.findAllByTvSeriesId(map.getId());
        if (!seasons.isEmpty()) {
            mapSeasonsToPageDTO(new HashSet<>(seasons), map);
        }
        return map;
    }

    private TvSeriesDetailsDTO mapToTvSeriesDetailsDTO(TvSeries tvSeries) {
        if (tvSeries == null) {
            return null;
        }

        TvSeriesDetailsDTO tvDetails = this.modelMapper.map(tvSeries, TvSeriesDetailsDTO.class);

        tvDetails.setGenres(mapGenres(tvSeries));
        tvDetails.setCast(this.castService.getCastByMediaId("tv", tvDetails.getId()));
        tvDetails.setCrew(this.crewService.getCrewByMediaId("tv", tvDetails.getId()));
        tvDetails.setProductionCompanies(this.productionCompanyService.mapProductionCompanies(tvSeries));
        sortSeasons(tvDetails);

        return tvDetails;
    }

    private Set<GenrePageDTO> mapGenres(TvSeries tvSeries) {
        return tvSeries.getGenres()
                .stream()
                .sorted(Comparator.comparing(SeriesGenre::getId))
                .map(genre -> this.modelMapper.map(genre, GenrePageDTO.class))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void sortSeasons(TvSeriesDetailsDTO tv) {
        tv.setSeasons(tv.getSeasons()
                .stream()
                .sorted(Comparator.comparingInt(SeasonTvSeriesDTO::getSeasonNumber))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    //    @Scheduled(fixedDelay = 100000000)
    //TODO
    private void updateTvSeries() {
    }

    private LocalDate getStartOfCurrentMonth() {
        return LocalDate.now().minusDays(7);
    }

    private void fetchSeries() {
        logger.info("Starting to fetch tv series...");

        int year = START_YEAR;

        int page = 1;
        Long countNewestTvSeries = this.tvSeriesRepository.countNewestTvSeries();
        int count = countNewestTvSeries.intValue();

        if (count > 0) {
            year = this.tvSeriesRepository.findNewestTvSeriesYear();

            if (count >= MAX_TV_SERIES_PER_YEAR) {
                year += 1;
                count = 0;
            } else {
                page = (int) Math.ceil(count / API_TV_SERIES_PER_PAGE);
            }
        }

        if (year == LocalDate.now().getYear() + 1) {
            return;
        }

        while (count < MAX_TV_SERIES_PER_YEAR) {
            logger.info("TV series - Fetching page {} of year {}", page, year);

            TvSeriesResponseApiDTO response = getTvSeriesResponseByDateAndVoteCount(page, year);

            if (response == null || response.getResults() == null) {
                logger.warn("No results returned for page {} of year {}", page, year);
                break;
            }

            if (page > response.getTotalPages()) {
                logger.info("Reached the last page for year {}.", year);
                if (year == LocalDate.now().getYear()) {
                    return;
                }
                year += 1;
                page = 1;
                count = 0;
                continue;
            }

            for (TvSeriesApiDTO dto : response.getResults()) {

                if (count >= MAX_TV_SERIES_PER_YEAR) {
                    break;
                }

                if (isInvalid(dto)) {
                    logger.warn("Invalid TV series: {}", dto.getId());
                    continue;
                }

                if (this.tvSeriesRepository.findByApiId(dto.getId()).isPresent()) {
                    logger.info("TV series already exists: {}", dto.getId());
                    continue;
                }

                TvSeriesApiByIdResponseDTO responseById = getTvSeriesResponseById(dto.getId());

                if (responseById == null) {
                    continue;
                }


                TrailerResponseApiDTO responseTrailer = this.trailerMappingUtil.getTrailerResponseById(dto.getId(),
                        this.apiConfig.getUrl(),
                        this.apiConfig.getKey(),
                        "tv");

                TvSeries tvSeries = tvSeriesMapper.mapToTvSeries(dto, responseById, responseTrailer);

                Map<String, Set<ProductionCompany>> productionCompaniesMap = productionCompanyService.getProductionCompaniesFromResponse(responseById, tvSeries);
                tvSeries.setProductionCompanies(productionCompaniesMap.get("all"));

                if (!productionCompaniesMap.get("toSave").isEmpty()) {
                    this.productionCompanyService.saveAllProduction(productionCompaniesMap.get("toSave"));
                }

                SeasonTvSeriesResponseApiDTO seasonsResponse = getSeasonsResponse(dto.getId());
                tvSeries.setSeasons(mapSeasonsAndEpisodesFromResponse(seasonsResponse, tvSeries));

                this.tvSeriesRepository.save(tvSeries);
                count++;

                logger.info("Saved tv series: {}", tvSeries.getName());

                List<CrewApiDTO> crewDto = responseById.getCrew().stream().limit(6).toList();
                Set<Crew> crewSet = this.crewService.mapToSet(crewDto);
                processTvSeriesCrew(crewDto, tvSeries, crewSet);

                MediaResponseCreditsDTO creditsById = getCreditsById(tvSeries.getApiId());
                if (creditsById == null) {
                    continue;
                }

                List<CastApiApiDTO> castDto = this.castService.filterCastApiDto(creditsById);
                Set<Cast> castSet = this.castService.mapToSet(castDto);
                processTvSeriesCast(castDto, tvSeries, castSet);
            }
            page++;
        }
        logger.info("Finished fetching tvSeries.");
    }

    private static boolean isInvalid(TvSeriesApiDTO dto) {
        return dto.getPosterPath() == null || dto.getPosterPath().isBlank()
                || dto.getOverview() == null || dto.getOverview().isBlank()
                || dto.getName() == null || dto.getName().isBlank()
                || dto.getBackdropPath() == null || dto.getBackdropPath().isBlank();
    }

    private static void mapSeasonsToPageDTO(Set<SeasonTvSeries> seasons, TvSeriesDTO map) {
        seasons.stream()
                .max(Comparator.comparing(SeasonTvSeries::getSeasonNumber))
                .ifPresent(lastSeason -> {
                    map.setSeasonsCount(lastSeason.getSeasonNumber() > 411
                            ? lastSeason.getSeasonNumber()
                            : seasons.size());
                    map.setEpisodesCount(lastSeason.getEpisodeCount());
                });
    }

    private Set<SeasonTvSeries> mapSeasonsAndEpisodesFromResponse(SeasonTvSeriesResponseApiDTO seasonsResponse, TvSeries tvSeries) {
        if (seasonsResponse == null || tvSeries == null) {
            return new HashSet<>();
        }

        Set<SeasonTvSeries> seasons = new HashSet<>();

        for (SeasonDTO seasonDTO : seasonsResponse.getSeasons()) {
            if (seasonDTO.getAirDate() == null || seasonDTO.getSeasonNumber() < 1) {
                continue;
            }

            if (this.seasonTvSeriesRepository.findByApiId(seasonDTO.getId()).isEmpty()) {
                SeasonTvSeries season = new SeasonTvSeries();
                season.setApiId(seasonDTO.getId());
                season.setSeasonNumber(seasonDTO.getSeasonNumber());
                season.setAirDate(seasonDTO.getAirDate());
                season.setEpisodeCount(seasonDTO.getEpisodeCount());
                season.setPosterPath(seasonDTO.getPosterPath());
                season.setTvSeries(tvSeries);
                season.setEpisodes(mapEpisodesFromResponse(tvSeries.getApiId(), season));
                seasons.add(season);
            }
        }
        return seasons;
    }

    private Set<EpisodeTvSeries> mapEpisodesFromResponse(long id, SeasonTvSeries season) {
        EpisodesTvSeriesResponseDTO episodesResponse = getEpisodesResponse(id, season.getSeasonNumber());

        if (episodesResponse == null) {
            return new HashSet<>();
        }

        return episodesResponse.getEpisodes()
                .stream()
                .map(dto -> {
                    EpisodeTvSeries map = this.modelMapper.map(dto, EpisodeTvSeries.class);
                    map.setSeason(season);
                    return map;
                })
                .collect(Collectors.toSet());

    }

    private void mapOneGenreToPageDTO(TvSeriesPageWithGenreDTO map) {
        Optional<SeriesGenre> optional = this.seriesGenreService.getAllGenresByMovieId(map.getId()).stream().findFirst();
        optional.ifPresent(genre -> map.setGenre(genre.getName()));
    }

    private void mapAllGenresToPageDTO(TvSeriesTrendingPageDTO map) {
        List<SeriesGenre> genres = this.seriesGenreService.getAllGenresByMovieId(map.getId()).stream().toList();
        if (!genres.isEmpty()) {
            map.setAllGenres(genres
                    .stream()
                    .map(genre -> this.modelMapper.map(genre, GenrePageDTO.class)).toList());
        }
    }

    private void processTvSeriesCast(List<CastApiApiDTO> castDto, TvSeries tvSeries, Set<Cast> castSet) {
        this.castService.processCast(
                castDto,
                tvSeries,
                c -> castTvSeriesRepository.findByTvSeriesIdAndCastApiIdAndCharacter(tvSeries.getId(), c.getId(), c.getCharacter()),
                (c, t) -> castService.createCastEntity(
                        c,
                        t,
                        castSet,
                        CastTvSeries::new,
                        CastTvSeries::setTvSeries,
                        CastTvSeries::setCast,
                        CastTvSeries::setCharacter
                ),
                castTvSeriesRepository::save
        );
    }

    private void processTvSeriesCrew(List<CrewApiDTO> crewDto, TvSeries tvSeries, Set<Crew> crewSet) {
        this.crewService.processCrew(
                crewDto,
                tvSeries,
                c -> crewTvSeriesRepository.findByTvSeriesIdAndCrewApiIdAndJobJob(tvSeries.getId(), c.getId(), "Creator"),
                (c, t) -> {
                    CrewTvSeries crewTvSeries = new CrewTvSeries();
                    crewTvSeries.setTvSeries(t);
                    return crewTvSeries;
                },
                crewTvSeriesRepository::save,
                c -> "Creator",
                crewSet
        );
    }

    private TvSeriesResponseApiDTO getTvSeriesResponseByDateAndVoteCount(int page, int year) {
        String url = String.format(this.apiConfig.getUrl()
                        + "/discover/tv?first_air_date.gte=%d-01-01&first_air_date.lte=%d-12-31&sort_by=vote_count.desc&api_key=%s&page=%d",
                year, year, this.apiConfig.getKey(), page);

        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(TvSeriesResponseApiDTO.class);
        } catch (Exception e) {
            System.err.println("Error fetching tv-series" + "- " + e.getMessage());
            return null;
        }
    }

    private SeasonTvSeriesResponseApiDTO getSeasonsResponse(Long id) {
        String url = String.format(this.apiConfig.getUrl() + "/tv/%d?api_key=%s", id, this.apiConfig.getKey());

        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(SeasonTvSeriesResponseApiDTO.class);
        } catch (Exception e) {
            System.err.println("Error fetching season for tv-series with ID: " + id + " - " + e.getMessage());
            return null;
        }
    }

    private EpisodesTvSeriesResponseDTO getEpisodesResponse(Long tvId, Integer season) {
        String url = String.format(this.apiConfig.getUrl() + "/tv/%d/season/%d?api_key=%s", tvId, season, this.apiConfig.getKey());

        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(EpisodesTvSeriesResponseDTO.class);
        } catch (Exception e) {
            System.err.println("Error fetching season for tv-series with ID: " + tvId + " - " + e.getMessage());
            return null;
        }
    }

    private TvSeriesApiByIdResponseDTO getTvSeriesResponseById(Long apiId) {
        String url = String.format(this.apiConfig.getUrl() + "/tv/%d?api_key=" + this.apiConfig.getKey(), apiId);
        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(TvSeriesApiByIdResponseDTO.class);
        } catch (Exception e) {
            System.err.println("Error fetching tv-series with ID: " + apiId + " - " + e.getMessage());
            return null;
        }
    }

    private MediaResponseCreditsDTO getCreditsById(Long apiId) {
        String url = String.format(this.apiConfig.getUrl() + "/tv/%d/credits?api_key=%s", apiId, this.apiConfig.getKey());

        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(MediaResponseCreditsDTO.class);
        } catch (Exception e) {
            System.err.println("Error fetching credits with ID: " + apiId + " - " + e.getMessage());
            return null;
        }
    }
}

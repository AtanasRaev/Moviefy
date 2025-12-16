package com.moviefy.service.scheduling;

import com.moviefy.config.cache.events.byGenres.ByGenresChangedMediaEvent;
import com.moviefy.config.cache.events.byGenres.ByGenresChangedMoviesEvent;
import com.moviefy.config.cache.events.byGenres.ByGenresChangedTvSeriesEvent;
import com.moviefy.config.cache.events.cast.CastByMediaChangedEvent;
import com.moviefy.config.cache.events.cast.CastByMovieChangedEvent;
import com.moviefy.config.cache.events.cast.CastByTvSeriesChangedEvent;
import com.moviefy.config.cache.events.collection.CollectionChangedEvent;
import com.moviefy.config.cache.events.collection.MoviesByCollectionChangedEvent;
import com.moviefy.config.cache.events.crew.CrewByMediaChangedEvent;
import com.moviefy.config.cache.events.crew.CrewByMovieChangedEvent;
import com.moviefy.config.cache.events.crew.CrewByTvSeriesChangedEvent;
import com.moviefy.config.cache.events.details.MoviesDetailsByCollectionChangedEvent;
import com.moviefy.config.cache.events.details.MoviesDetailsChangedEvent;
import com.moviefy.config.cache.events.details.TvSeriesDetailsChangedEvent;
import com.moviefy.config.cache.events.latest.LatestChangedMediaEvent;
import com.moviefy.config.cache.events.latest.LatestChangedMoviesEvent;
import com.moviefy.config.cache.events.latest.LatestChangedTvSeriesEvent;
import com.moviefy.config.cache.events.trending.TrendingChangedMediaEvent;
import com.moviefy.config.cache.events.trending.TrendingChangedMoviesEvent;
import com.moviefy.config.cache.events.trending.TrendingChangedTvSeriesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class MediaEventPublisher {
    private final ApplicationEventPublisher events;

    public MediaEventPublisher(ApplicationEventPublisher events) {
        this.events = events;
    }

    public void publishLatestMoviesChangedEvent() {
        this.events.publishEvent(new LatestChangedMoviesEvent());
    }

    public void publishLatestSeriesChangedEvent() {
        this.events.publishEvent(new LatestChangedTvSeriesEvent());
    }

    public void publishLatestMediaChangedEvent() {
        this.events.publishEvent(new LatestChangedMediaEvent());
    }

    public void publishTrendingMoviesChangedEvent() {
        this.events.publishEvent(new TrendingChangedMoviesEvent());
    }

    public void publishTrendingSeriesChangedEvent() {
        this.events.publishEvent(new TrendingChangedTvSeriesEvent());
    }

    public void publishTrendingMediaChangedEvent() {
        this.events.publishEvent(new TrendingChangedMediaEvent());
    }

    public void publishByGenresChangedMoviesEvent() {
        this.events.publishEvent(new ByGenresChangedMoviesEvent());
    }

    public void publishByGenresChangedTvSeriesEvent() {
        this.events.publishEvent(new ByGenresChangedTvSeriesEvent());
    }

    public void publishByGenresChangedMediaEvent() {
        this.events.publishEvent(new ByGenresChangedMediaEvent());
    }

    public void publishCollectionChangedEvent(String collectionName) {
        events.publishEvent(new CollectionChangedEvent(collectionName));
    }

    public void publishMoviesByCollectionChangedEvent(Long collectionApiId) {
        events.publishEvent(new MoviesByCollectionChangedEvent(collectionApiId));
    }

    public void publishMoviesDetailsChangedEvent(List<Long> ids) {
        this.events.publishEvent(new MoviesDetailsChangedEvent(List.copyOf(ids)));
    }

    public void publishMoviesDetailsByCollectionChangedEvent(Long collectionApiId) {
        events.publishEvent(new MoviesDetailsByCollectionChangedEvent(collectionApiId));
    }

    public void publishTvSeriesDetailsChangedEvent(List<Long> ids) {
        this.events.publishEvent(new TvSeriesDetailsChangedEvent(List.copyOf(ids)));
    }

    public void publishCastByMovieChangedEvent(Set<Long> castIds) {
        events.publishEvent(new CastByMovieChangedEvent(Set.copyOf(castIds)));
    }

    public void publishCastByTvSeriesChangedEvent(Set<Long> castIds) {
        events.publishEvent(new CastByTvSeriesChangedEvent(Set.copyOf(castIds)));
    }

    public void publishCastByMediaChangedEvent(Set<Long> castIds) {
        events.publishEvent(new CastByMediaChangedEvent(Set.copyOf(castIds)));
    }

    public void publishCrewByMovieChangedEvent(Set<Long> crewIds) {
        events.publishEvent(new CrewByMovieChangedEvent(Set.copyOf(crewIds)));
    }

    public void publishCrewByTvSeriesChangedEvent(Set<Long> crewIds) {
        events.publishEvent(new CrewByTvSeriesChangedEvent(Set.copyOf(crewIds)));
    }

    public void publishCrewByMediaChangedEvent(Set<Long> crewIds) {
        events.publishEvent(new CrewByMediaChangedEvent(Set.copyOf(crewIds)));
    }
}


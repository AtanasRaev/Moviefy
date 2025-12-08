package com.moviefy.service.ingest;

import com.moviefy.config.cache.events.LatestChangedMediaEvent;
import com.moviefy.config.cache.events.LatestChangedMoviesEvent;
import com.moviefy.config.cache.events.LatestChangedTvSeriesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MediaEventPublisher {
    private final ApplicationEventPublisher events;

    public MediaEventPublisher(ApplicationEventPublisher events) {
        this.events = events;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishLatestMoviesChangedEvent() {
        events.publishEvent(new LatestChangedMoviesEvent());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishLatestSeriesChangedEvent() {
        events.publishEvent(new LatestChangedTvSeriesEvent());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishLatestMediaChangedEvent() {
        events.publishEvent(new LatestChangedMediaEvent());
    }
}


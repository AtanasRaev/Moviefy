package com.moviefy.config.cache.events.details;

import java.util.List;

public record MoviesDetailsChangedEvent(List<Long> ids) {
}


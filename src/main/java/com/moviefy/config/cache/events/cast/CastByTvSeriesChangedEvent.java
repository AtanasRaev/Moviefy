package com.moviefy.config.cache.events.cast;

import java.util.Set;

public record CastByTvSeriesChangedEvent(Set<Long> castIds) {
}

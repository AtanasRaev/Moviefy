package com.moviefy.config.cache.events.cast;

import java.util.Set;

public record CastByMovieChangedEvent(Set<Long> castIds) {}


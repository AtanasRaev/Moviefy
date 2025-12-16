package com.moviefy.config.cache.events.crew;

import java.util.Set;

public record CrewByMovieChangedEvent(Set<Long> crewIds) {
}

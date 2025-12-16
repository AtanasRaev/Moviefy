package com.moviefy.config.cache.events.crew;

import java.util.Set;

public record CrewByMediaChangedEvent(Set<Long> crewIds) {
}

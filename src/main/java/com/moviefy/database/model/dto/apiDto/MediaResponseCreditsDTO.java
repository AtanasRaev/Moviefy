package com.moviefy.database.model.dto.apiDto;

import java.util.Set;

public class MediaResponseCreditsDTO {
    Set<CastApiApiDTO> cast;

    Set<CrewApiDTO> crew;

    public Set<CastApiApiDTO> getCast() {
        return cast;
    }

    public void setCast(Set<CastApiApiDTO> cast) {
        this.cast = cast;
    }

    public Set<CrewApiDTO> getCrew() {
        return crew;
    }

    public void setCrew(Set<CrewApiDTO> crew) {
        this.crew = crew;
    }
}

package com.watchitnow.database.model.dto.apiDto;

import java.util.Set;

public class MediaResponseCreditsDTO {
    Set<CastApiApiDTO> cast;

    Set<CrewApiApiDTO> crew;

    public Set<CastApiApiDTO> getCast() {
        return cast;
    }

    public void setCast(Set<CastApiApiDTO> cast) {
        this.cast = cast;
    }

    public Set<CrewApiApiDTO> getCrew() {
        return crew;
    }

    public void setCrew(Set<CrewApiApiDTO> crew) {
        this.crew = crew;
    }
}

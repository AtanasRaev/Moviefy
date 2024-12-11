package com.moviefy.utils;

import com.moviefy.database.model.dto.apiDto.CrewApiDTO;
import com.moviefy.database.model.entity.credit.crew.Crew;
import org.springframework.stereotype.Component;

@Component
public class CrewMapper extends CreditMapper {
    public Crew mapToCrew(CrewApiDTO dto) {
        Crew crew = new Crew();
        super.mapCommonFields(crew, dto);
        return crew;
    }
}

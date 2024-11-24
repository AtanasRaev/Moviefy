package com.watchitnow.utils;

import com.watchitnow.database.model.dto.apiDto.CrewApiDTO;
import com.watchitnow.database.model.entity.credit.Crew.Crew;
import org.springframework.stereotype.Component;

@Component
public class CrewMapper extends CreditMapper {
    public Crew mapToCrew(CrewApiDTO dto) {
        Crew crew = new Crew();
        super.mapCommonFields(crew, dto);
        return crew;
    }
}

package com.watchitnow.utils;

import com.watchitnow.database.model.dto.apiDto.CrewApiApiDTO;
import com.watchitnow.database.model.entity.credit.Crew.Crew;
import com.watchitnow.database.model.entity.media.Media;
import org.springframework.stereotype.Component;

@Component
public class CrewMapper extends CreditMapper {
    public Crew mapToCrew(CrewApiApiDTO dto, Media media) {
        Crew crew = new Crew();
        super.mapCommonFields(crew, dto);
        return crew;
    }
}

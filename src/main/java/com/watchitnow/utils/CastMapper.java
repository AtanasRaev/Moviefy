package com.watchitnow.utils;

import com.watchitnow.database.model.dto.apiDto.CastApiApiDTO;
import com.watchitnow.database.model.entity.credit.cast.Cast;
import org.springframework.stereotype.Component;

@Component
public class CastMapper extends CreditMapper {
    public Cast mapToCast(CastApiApiDTO dto) {
        Cast cast = new Cast();

        super.mapCommonFields(cast, dto);
        return cast;
    }
}

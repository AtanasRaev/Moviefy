package com.moviefy.utils.mappers;

import com.moviefy.database.model.dto.apiDto.creditDto.CastApiDTO;
import com.moviefy.database.model.entity.credit.cast.Cast;
import org.springframework.stereotype.Component;

@Component
public class CastMapper extends CreditMapper {
    public Cast mapToCast(CastApiDTO dto) {
        Cast cast = new Cast();

        super.mapCommonFields(cast, dto);
        return cast;
    }
}

package com.moviefy.utils;

import com.moviefy.database.model.dto.apiDto.CreditApiDTO;
import com.moviefy.database.model.entity.credit.Credit;

public abstract class CreditMapper {

    protected void mapCommonFields(Credit credit, CreditApiDTO dto) {
        credit.setApiId(dto.getId());
        credit.setName(dto.getName());
        credit.setProfilePath(dto.getProfilePath() == null || dto.getProfilePath().isBlank() ? null : dto.getProfilePath());
    }
}

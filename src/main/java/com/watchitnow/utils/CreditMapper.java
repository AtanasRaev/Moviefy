package com.watchitnow.utils;

import com.watchitnow.database.model.dto.apiDto.CreditApiDTO;
import com.watchitnow.database.model.entity.credit.Credit;

public abstract class CreditMapper {

    protected void mapCommonFields(Credit credit, CreditApiDTO dto) {
        credit.setApiId(dto.getId());
        credit.setName(dto.getName());
        credit.setProfilePath(dto.getProfilePath() == null || dto.getProfilePath().isBlank() ? null : dto.getProfilePath());
    }
}

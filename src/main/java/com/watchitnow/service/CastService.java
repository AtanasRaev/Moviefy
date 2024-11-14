package com.watchitnow.service;

import com.watchitnow.database.model.dto.apiDto.CastApiApiDTO;
import com.watchitnow.database.model.dto.apiDto.MediaResponseCreditsDTO;
import com.watchitnow.database.model.entity.credit.Cast.Cast;
import com.watchitnow.database.model.entity.media.Media;

import java.util.List;
import java.util.Set;

public interface CastService {
    List<Cast> findAllByApiId(List<Long> apiIds);

    void saveAll(Set<Cast> cast);

    Set<Cast> mapToSet(List<CastApiApiDTO> castDto, Media media);

    List<CastApiApiDTO> filterCastApiDto(MediaResponseCreditsDTO creditsById);
}

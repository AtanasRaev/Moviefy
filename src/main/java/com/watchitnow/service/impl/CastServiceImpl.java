package com.watchitnow.service.impl;

import com.watchitnow.database.model.dto.apiDto.CastApiApiDTO;
import com.watchitnow.database.model.dto.apiDto.CreditApiDTO;
import com.watchitnow.database.model.dto.apiDto.MediaResponseCreditsDTO;
import com.watchitnow.database.model.entity.credit.Cast.Cast;
import com.watchitnow.database.model.entity.credit.Credit;
import com.watchitnow.database.model.entity.media.Media;
import com.watchitnow.database.repository.CastRepository;
import com.watchitnow.service.CastService;
import com.watchitnow.utils.CastMapper;
import com.watchitnow.utils.CreditRetrievalUtil;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CastServiceImpl implements CastService {
    private final CastRepository castRepository;
    private final CreditRetrievalUtil creditRetrievalUtil;
    private final CastMapper castMapper;

    public CastServiceImpl(CastRepository castRepository,
                           CreditRetrievalUtil creditRetrievalUtil,
                           CastMapper castMapper) {
        this.castRepository = castRepository;
        this.creditRetrievalUtil = creditRetrievalUtil;
        this.castMapper = castMapper;
    }

    @Override
    public List<Cast> findAllByApiId(List<Long> apiIds) {
        return this.castRepository.findAllByApiIds(apiIds);
    }

    @Override
    public void saveAll(Set<Cast> cast) {
        this.castRepository.saveAll(cast);
    }

    @Override
    public Set<Cast> mapToSet(List<CastApiApiDTO> castDto, Media movie) {
        return creditRetrievalUtil.creditRetrieval(
                castDto,
                cast -> this.castMapper.mapToCast(cast, movie),
                CreditApiDTO::getId,
                Credit::getApiId,
                this::findAllByApiId,
                savedCast -> {
                    this.saveAll(savedCast);
                    return null;
                }
        );
    }

    @Override
    public List<CastApiApiDTO> filterCastApiDto(MediaResponseCreditsDTO creditsById) {
        Map<Long, String> uniqueIds = new HashMap<>();
        return creditsById.getCast()
                .stream()
                .filter(cast -> cast.getName() != null && !cast.getName().isBlank()
                        && cast.getCharacter() != null && !cast.getCharacter().isBlank())
                .filter(cast -> {
                    if (uniqueIds.containsKey(cast.getId()) && uniqueIds.get(cast.getId()).equals(cast.getCharacter())) {
                        return false;
                    }
                    uniqueIds.put(cast.getId(), cast.getCharacter());
                    return true;
                })
                .sorted(Comparator.comparing(CastApiApiDTO::getOrder))
                .limit(10)
                .toList();
    }
}

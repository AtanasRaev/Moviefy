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

import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
                Credit::getId,
                this::findAllByApiId,
                savedCast -> {
                    this.saveAll(savedCast);
                    return null;
                }
        );
    }

    @Override
    public List<CastApiApiDTO> filterCastApiDto(MediaResponseCreditsDTO creditsById) {
        return creditsById.getCast()
                .stream()
                .filter(crew -> crew.getName() != null && !crew.getName().isBlank())
                .sorted(Comparator.comparing(CastApiApiDTO::getOrder))
                .limit(10)
                .toList();
    }
}

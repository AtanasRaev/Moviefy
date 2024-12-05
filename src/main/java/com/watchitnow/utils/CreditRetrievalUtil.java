package com.watchitnow.utils;

import com.watchitnow.config.ApiConfig;
import com.watchitnow.database.model.dto.apiDto.MediaResponseCreditsDTO;
import com.watchitnow.database.model.dto.pageDto.CastPageDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class CreditRetrievalUtil {
    public <T, R> Set<R> creditRetrieval(List<T> dtoList,
                                         Function<T, R> mapFunction,
                                         Function<T, Long> dtoIdExtraction,
                                         Function<R, Long> CreditApiIdExtraction,
                                         Function<List<Long>, List<R>> findAllByApiId,
                                         Function<Set<R>, Void> saveCredit) {
        Set<Long> dtoIdsList = dtoList.stream().map(dtoIdExtraction).collect(Collectors.toSet());

        Set<R> creditList = new LinkedHashSet<>(findAllByApiId.apply(dtoIdsList.stream().toList()));

        if (creditList.size() != dtoIdsList.size()) {
            Set<Long> uniqueIds = new HashSet<>();
            Set<Long> existingCreditIds = creditList.stream().map(CreditApiIdExtraction).collect(Collectors.toSet());

            Set<T> newCreditDto = dtoList.stream()
                    .filter(item -> !existingCreditIds.contains(dtoIdExtraction.apply(item)))
                    .filter(cast -> uniqueIds.add(dtoIdExtraction.apply(cast)))
                    .collect(Collectors.toSet());

            Set<R> newCredit = newCreditDto.stream()
                    .map(mapFunction)
                    .collect(Collectors.toSet());

            creditList.addAll(newCredit);


            Set<R> creditSet = new LinkedHashSet<>(creditList);
            saveCredit.apply(creditSet);
        }
        return new LinkedHashSet<>(creditList);
    }

    public <T, R> Set<R> getCreditByMediaId(Long mediaId,
                                            Supplier<R> creditSupplier,
                                            Function<Long, List<T>> findCreditByMovieId,
                                            BiConsumer<R, Long> mapId,
                                            BiConsumer<R, String> mapField,
                                            BiConsumer<R, String> mapName,
                                            BiConsumer<R, String> mapProfilePath,
                                            Function<T, Long> getId,
                                            Function<T, String> getFiled,
                                            Function<T, String> getName,
                                            Function<T, String> getProfilePath) {

        return findCreditByMovieId.apply(mediaId)
                .stream()
                .map(c -> {
                    R credit = creditSupplier.get();
                    mapId.accept(credit, getId.apply(c));
                    mapField.accept(credit, getFiled.apply(c));
                    mapName.accept(credit, getName.apply(c));
                    mapProfilePath.accept(credit, getProfilePath.apply(c));
                    return credit;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

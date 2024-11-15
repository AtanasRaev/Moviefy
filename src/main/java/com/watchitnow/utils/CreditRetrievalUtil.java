package com.watchitnow.utils;

import com.watchitnow.config.ApiConfig;
import com.watchitnow.database.model.dto.apiDto.MediaResponseCreditsDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CreditRetrievalUtil {
    private final RestClient restClient;
    private final ApiConfig apiConfig;

    public CreditRetrievalUtil(RestClient restClient,
                               ApiConfig apiConfig) {
        this.restClient = restClient;
        this.apiConfig = apiConfig;
    }

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

    public MediaResponseCreditsDTO getCreditsById(Long apiId, String type) {
        String url = String.format(this.apiConfig.getUrl() + "/%s/%d/credits?api_key=%s", type, apiId, this.apiConfig.getKey());

        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(MediaResponseCreditsDTO.class);
        } catch (Exception e) {
            System.err.println("Error fetching credits with ID: " + apiId + " - " + e.getMessage());
            return null;
        }
    }
}

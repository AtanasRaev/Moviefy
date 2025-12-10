package com.moviefy.utils;

import com.moviefy.database.model.dto.apiDto.MediaApiDTO;
import com.moviefy.database.model.entity.media.Media;

public class EntityComparator {
    public static boolean isBetter(MediaApiDTO cand, Media worst) {
        int voteCmp = Integer.compare(safeInt(cand.getVoteCount()), safeInt(worst.getVoteCount()));
        if (voteCmp != 0) {
            return voteCmp > 0;
        }
        int popCmp = Double.compare(safeDouble(cand.getPopularity()), safeDouble(worst.getPopularity()));
        if (popCmp != 0) {
            return popCmp > 0;
        }
        return cand.getId() < worst.getApiId();
    }

    private static int safeInt(Integer x) {
        return x == null ? 0 : x;
    }

    private static double safeDouble(Double x) {
        return x == null ? 0.0 : x;
    }
}

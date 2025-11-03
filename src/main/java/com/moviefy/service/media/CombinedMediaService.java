package com.moviefy.service.media;

import java.util.List;
import java.util.Map;

public interface CombinedMediaService {
    Map<String, Object> getCombinedMediaByGenres(List<String> genres, int page, int size);
}

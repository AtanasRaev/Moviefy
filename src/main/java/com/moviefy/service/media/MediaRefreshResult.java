package com.moviefy.service.media;

import com.moviefy.database.model.dto.detailsDto.MediaDetailsDTO;

public record MediaRefreshResult(MediaRefreshStatus status, MediaDetailsDTO media) {
    public static MediaRefreshResult updated(MediaDetailsDTO media) {
        return new MediaRefreshResult(MediaRefreshStatus.UPDATED, media);
    }

    public static MediaRefreshResult unchanged(MediaDetailsDTO media) {
        return new MediaRefreshResult(MediaRefreshStatus.UNCHANGED, media);
    }

    public static MediaRefreshResult notFoundLocal() {
        return new MediaRefreshResult(MediaRefreshStatus.NOT_FOUND_LOCAL, null);
    }

    public static MediaRefreshResult notFoundExternal() {
        return new MediaRefreshResult(MediaRefreshStatus.NOT_FOUND_EXTERNAL, null);
    }

    public static MediaRefreshResult failed() {
        return new MediaRefreshResult(MediaRefreshStatus.FAILED, null);
    }
}

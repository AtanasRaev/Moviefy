package com.moviefy.config.cache;

public final class RefreshConfig {
    public static final int REFRESH_CAP = 200;
    public static final int TRENDING_CAP = 80;
    public static final int DAYS_CAP = 60;
    public static final int DAYS_GUARD = 3;
    public static final int COOL_DOWN_DAYS = 10;

    private RefreshConfig() {}
}

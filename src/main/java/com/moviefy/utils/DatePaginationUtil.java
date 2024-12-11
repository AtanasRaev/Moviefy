package com.moviefy.utils;

import java.time.LocalDate;

public class DatePaginationUtil {

    public static DateRange updatePageAndDate(int page, int totalPages, int i, int savedSeriesCount, LocalDate startDate, LocalDate endDate, int year) {
        if ((page >= totalPages || page == 500) || (i == 39 && savedSeriesCount == 0)) {
            page = 1;
            startDate = startDate.minusMonths(1);

            if (startDate.getMonthValue() == 1) {
                year--;
                startDate = LocalDate.of(year, 12, 1);
            }

            endDate = startDate.plusMonths(1).minusDays(1);

            if (i == 39 && savedSeriesCount == 0) {
                i--;
            }
            return new DateRange(page, startDate, endDate, year, true);
        } else {
            page++;
            return new DateRange(page, startDate, endDate, year, false);
        }
    }
}


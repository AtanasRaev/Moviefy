package com.moviefy.utils;

import java.time.LocalDate;

public class DateRange {
    private int page;
    private LocalDate startDate;
    private LocalDate endDate;
    private int year;
    private boolean reset;

    public DateRange(int page, LocalDate startDate, LocalDate endDate, int year, boolean reset) {
        this.page = page;
        this.startDate = startDate;
        this.endDate = endDate;
        this.year = year;
        this.reset = reset;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isReset() {
        return reset;
    }

    public void setReset(boolean reset) {
        this.reset = reset;
    }
}
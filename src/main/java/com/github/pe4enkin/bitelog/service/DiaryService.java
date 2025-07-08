package com.github.pe4enkin.bitelog.service;

import javax.sql.DataSource;
import java.time.LocalDate;

public class DiaryService {
    private final DataSource dataSource;

    public DiaryService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean loadForDate(LocalDate date) {
        if (date == null)
            return false;
        return true;
    }
}
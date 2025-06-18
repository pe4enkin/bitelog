package com.github.pe4enkin.bitelog.service;

import java.time.LocalDate;

public class DiaryService {

    public DiaryService() {
    }

    public boolean loadForDate(LocalDate date) {
        if (date == null)
            return false;
        return true;
    }
}
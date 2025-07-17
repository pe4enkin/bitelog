package com.github.pe4enkin.bitelog.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateTimeFormatterUtil {

    private DateTimeFormatterUtil() {
        throw new UnsupportedOperationException("DateTimeFormatterUtil утилитарный класс и не может быть инстанцирован.");
    }

    public static final DateTimeFormatter DATE_DD_MM_YYYY = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter DATE_DD_MMMM_YYYY = DateTimeFormatter.ofPattern("d MMMM yyyy 'г.'", Locale.forLanguageTag("ru"));
    public static final DateTimeFormatter TIME_HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    public static String formatDateWithDots(LocalDate date) {
        return date.format(DATE_DD_MM_YYYY);
    }

    public static String formatDateVerbose(LocalDate date) {
        return date.format(DATE_DD_MMMM_YYYY);
    }

    public static String formatTimeCompact(LocalTime time) {
        return time.format(TIME_HH_MM);
    }

    public static String formatDateTime(LocalDate date, LocalTime time) {
        return formatDateWithDots(date) + " в " + formatTimeCompact(time);
    }
}
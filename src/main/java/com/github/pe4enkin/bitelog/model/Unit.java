package com.github.pe4enkin.bitelog.model;

public enum Unit {
    GRAM("гр", "грамм"),
    MILLILITER("мл", "миллилитр"),
    LITER("л", "литр"),
    PIECE("шт", "штука"),
    PACK("упак", "упаковка"),
    CUP("чш", "чашка"),
    TABLESPOON("ст.л", "столовая ложка"),
    TEASPOON("ч.л", "чайная ложка"),
    SLICE("кус", "кусок");

    private final String shortName;
    private final String fullName;

    Unit(String shortName, String fullName) {
        this.shortName = shortName;
        this.fullName = fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public String getFullName() {
        return fullName;
    }
}

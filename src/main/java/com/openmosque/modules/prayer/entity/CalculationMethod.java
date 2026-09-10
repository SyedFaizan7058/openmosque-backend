package com.openmosque.modules.prayer.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Islamic Prayer Calculation Methods aligned with astronomical standards and Aladhan API.
 */
@Getter
@RequiredArgsConstructor
public enum CalculationMethod {
    KARACHI(1, "University of Islamic Sciences, Karachi"),
    ISNA(2, "Islamic Society of North America (ISNA)"),
    MUSLIM_WORLD_LEAGUE(3, "Muslim World League (MWL)"),
    UMM_AL_QURA(4, "Umm Al-Qura University, Makkah"),
    EGYPTIAN(5, "Egyptian General Authority of Survey"),
    TEHRAN(7, "Institute of Geophysics, University of Tehran"),
    GULF(8, "Gulf Region"),
    KUWAIT(9, "Kuwait"),
    QATAR(10, "Qatar"),
    SINGAPORE(11, "Majlis Ugama Islam Singapura, Singapore"),
    FRANCE(12, "Union des Organisations Islamiques de France (UOIF)"),
    TURKEY(13, "Diyanet İşleri Başkanlığı, Turkey"),
    RUSSIA(14, "Spiritual Administration of Muslims of Russia"),
    CUSTOM(99, "Custom Angles & Offsets");

    private final int aladhanMethodId;
    private final String displayName;
}

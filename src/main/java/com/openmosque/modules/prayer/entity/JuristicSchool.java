package com.openmosque.modules.prayer.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Juristic method (School of Thought) for calculating Asr prayer time.
 */
@Getter
@RequiredArgsConstructor
public enum JuristicSchool {
    STANDARD(0, "Standard (Shafi, Maliki, Hanbali) - Shadow length 1x"),
    HANAFI(1, "Hanafi - Shadow length 2x (Later Asr)");

    private final int aladhanSchoolId;
    private final String description;
}

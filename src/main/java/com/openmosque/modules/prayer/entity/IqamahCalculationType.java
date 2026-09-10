package com.openmosque.modules.prayer.entity;

/**
 * Strategy for determining Iqamah (congregational prayer) time.
 */
public enum IqamahCalculationType {
    /**
     * Iqamah occurs a fixed number of minutes after the Adhan (e.g. Adhan + 20 mins).
     */
    OFFSET_AFTER_ADHAN,

    /**
     * Iqamah occurs at an exact fixed clock time (e.g. exactly 13:30).
     */
    FIXED_TIME
}

package pl.nitka.blazej.enums;

import lombok.Getter;

/**
 * @author Blazej
 */
public enum WorkingHolidayDayName {

    /**
     * Pracująca sobota.
     */
    SATURDAY(6),

    /**
     * Pracująca niedziela.
     */
    SUNDAY(7);

    @Getter
    private int dayOfWeekNumber;

    WorkingHolidayDayName(int dayOfWeekNumber) {
        this.dayOfWeekNumber = dayOfWeekNumber;
    }
}

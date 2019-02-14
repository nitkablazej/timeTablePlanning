package pl.nitka.blazej.utils;

import lombok.Getter;
import lombok.Setter;

/**
 * Klasa przechowująca tymczasowy obiekt przepracowanego okresu.
 *
 * @author Blazej
 */
@Getter
@Setter
class TempWorkTime {
    private int years;
    private int months;
    private int days;

    TempWorkTime(int years, int months, int days) {
        this.years = years;
        this.months = months;
        this.days = days;
    }
}
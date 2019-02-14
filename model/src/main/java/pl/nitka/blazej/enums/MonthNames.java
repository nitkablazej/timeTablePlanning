package pl.nitka.blazej.enums;

import lombok.Getter;

/**
 * Enum z tłumaczeniami nazw miesięcy
 *
 * @author Blazej
 */
public enum MonthNames {

    JANUARY("Styczeń", 0),
    FEBRUARY("Luty", 1),
    MARCH("Marzec", 2),
    APRIL("Kwiecień", 3),
    MAY("Maj", 4),
    JUNE("Czerwiec", 5),
    JULY("Lipiec", 6),
    AUGUST("Sierpień", 7),
    SEPTEMBER("Wrzesień", 8),
    OCTOBER("Październik", 9),
    NOVEMBER("Listopad", 10),
    DECEMBER("Grudzień", 11);

    @Getter
    private String translate;

    @Getter
    private int monthNumber;

    MonthNames(String translate, int monthNumber) {
        this.translate = translate;
        this.monthNumber = monthNumber;
    }

}

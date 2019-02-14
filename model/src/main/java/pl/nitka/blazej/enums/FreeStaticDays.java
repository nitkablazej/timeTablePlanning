package pl.nitka.blazej.enums;

import lombok.Getter;

/**
 * Lista dni świątecznych, które mają stałą datę w roku.
 *
 * @author Blazej
 */
@Getter
public enum FreeStaticDays {

    NEW_YEAR(1, 0, "Nowy Rok"),
    EPIPHANY(6, 0, "Święto Trzech Króli"),
    LABOUR_DAY(1, 4, "Święto Pracy"),
    CONSTITUTION_DAY(3, 4, "Święto Konstytucji"),
    ASSUMPTION_MARY(15, 7, "Wniebowzięcie Najświętszej Maryi Panny"),
    ALL_SAINTS(1, 10, "Wszystkich Świętych"),
    INDEPENDENCE_DAY(11, 10, "Dzień Niepodległości"),
    CHRISTMAS_DAY(25, 11, "Boże Narodzenie"),
    SECOND_CHRISTMAS_DAY(26, 11, "Drugi dzień świąt Bożego Narodzenia");

    /**
     * Dzień danego święta.
     */
    private int day;

    /**
     * Rok danego święta.
     */
    private int month;

    /**
     * Tekstowe tłumaczenie święta.
     */
    private String translate;

    FreeStaticDays(int day, int month, String translate) {
        this.day = day;
        this.month = month;
        this.translate = translate;
    }
}

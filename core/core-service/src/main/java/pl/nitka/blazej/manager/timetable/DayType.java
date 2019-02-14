package pl.nitka.blazej.manager.timetable;

/**
 * Typ generowanego dnia.
 *
 * @author Blazej
 */
public enum DayType {

    /**
     * Od poniedziałku do piątku (jeśli nie święto).
     */
    NORMAL,

    /**
     * Weekend (jeśli nie święto).
     */
    WEEKEND,

    /**
     * Święto.
     */
    FREE_DAY
}

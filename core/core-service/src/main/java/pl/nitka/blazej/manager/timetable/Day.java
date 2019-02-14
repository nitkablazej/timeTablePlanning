package pl.nitka.blazej.manager.timetable;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Klasa POJO reprezentująca pojedynczy dzień z generowanego miesiąca.
 *
 * @author Blazej
 */
public class Day {

    /**
     * Typ dnia (normalny / weekend / święto).
     */
    @Getter
    @Setter
    private DayType dayType;

    /**
     * Data dnia.
     */
    @Getter
    @Setter
    private Date date;

    /**
     * Numer dnia w tygodniu.
     */
    @Getter
    @Setter
    private int dayOfWeekNumber;

    /**
     * Lista pracowników z pierwszej zmiany.
     */
    @Getter
    private List<Integer> firstChangeEmployeesList;

    /**
     * Lista pracowników z drugiej zmiany.
     */
    @Getter
    private List<Integer> secondChangeEmployeesList;

    /**
     * Oznaczenie, czy jest to dzień pracujący.
     */
    @Getter
    @Setter
    private boolean workingDay;

    /**
     * Konstruktor.
     */
    public Day() {
        dayType = DayType.NORMAL;
        firstChangeEmployeesList = new ArrayList<>();
        secondChangeEmployeesList = new ArrayList<>();
        workingDay = true;
    }

    /**
     * Dodanie pracownika do pierwszej zmiany.
     *
     * @param employeeNumber - numer pracownika
     */
    public void addEmployeeNumberToFirstChange(Integer employeeNumber) {
        firstChangeEmployeesList.add(employeeNumber);
    }

    /**
     * Dodanie pracownika do drugiej zmiany.
     *
     * @param employeeNumber - numer pracownika
     */
    public void addEmployeeNumberToSecondChange(Integer employeeNumber) {
        secondChangeEmployeesList.add(employeeNumber);
    }
}

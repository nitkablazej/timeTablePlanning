package pl.nitka.blazej.manager.timetable;

import lombok.Getter;
import lombok.Setter;
import pl.nitka.blazej.entity.employee.Employee;

import java.util.Date;

/**
 * Klasa POJO reprezentująca obiekt uwzględnianego w grafiku pracownika.
 *
 * @author Blazej
 */
public class WorkingEmployee {

    /**
     * Pracownik.
     */
    @Getter
    @Setter
    private Employee employee;

    /**
     * Numer pracownika.
     */
    @Getter
    @Setter
    private int number;

    /**
     * Etykieta pracownika.
     */
    @Getter
    @Setter
    private String employeeLabel;

    /**
     * Data i czas ostatniego wyjścia z pracy.
     */
    @Getter
    @Setter
    private Date lastExitFromWork;

    /**
     * Przepracowane godziny z podstawowego zakresu pracy.
     */
    @Getter
    @Setter
    private int basicWorkingHours;

    /**
     * Przepracowane nadgodziny.
     */
    @Getter
    @Setter
    private int additionalWorkingHours;

    /**
     * Dodanie standardowych godzin pracy.
     */
    public void addBasicWorkingHours() {
        basicWorkingHours += 8;
    }

    /**
     * Dodanie nadgodzin.
     */
    public void addAdditionalWorkingHours() {
        additionalWorkingHours += 8;
    }

}

package pl.nitka.blazej.manager;

import pl.nitka.blazej.entity.employee.Employee;

import java.util.Date;
import java.util.List;

/**
 * Interfejs udostępniający innym modułom metody do zarządzania pracownikami.
 *
 * @author Blazej
 */
public interface EmployeeManager {

    /**
     * Metoda pozwalająca zapisać pracownika do bazy danych.
     *
     * @param employee - zapisywany pracownik
     */
    void saveEmployee(Employee employee);

    /**
     * Metoda zwracająca listę wszytkich pracowników z bazy.
     *
     * @return - lista wszystkich pracowników.
     */
    List<Employee> getAll();

    /**
     * Metoda wyszukująca pracownika po ID bazodanowym.
     *
     * @param employeeId - id pracownika
     * @return - pracownik
     */
    Employee findEmployee(long employeeId);

    /**
     * Metoda usuwająca pracownika wg id bazodanowego.
     *
     * @param employeeId - id bazodanowe pracownika
     */
    void deleteEmployee(long employeeId);

    /**
     * Metoda obliczająca dostępne dni urlopowe w obecnym roku.
     *
     * @return - liczba dni urlopu do wykorzystania na bieżący rok
     * @param employeeId - id pracownika
     */
    int calculateThisYearAvailableVacation(long employeeId);

    /**
     * Metoda obliczająca dostępne dni urlopowe w przyszłym roku.
     *
     * @return - liczba dni urlopu do wykorzystania na bieżący rok
     * @param employeeId - id pracownika
     */
    int calculateNextYearAvailableVacation(long employeeId);

    /**
     * Metoda sprawdzająca, czy pracownik nie ma już zarezerwowanego urlopu w danym dniu.
     *
     * @param dateFromVacation - data rozpoczęcia urlopu
     * @param dateToVacation - data zakończenia urlopu
     * @return - true, jeśli pracownik ma już urlop w danym zakresie
     */
    boolean employeeHasVacationInThisScope(Date dateFromVacation, Date dateToVacation, long employeeId);

    /**
     * Metoda zwracająca informację, czy w systemie nie istnieje już inny pracownik z takim samym identyfikatorem.
     *
     * @param uniqueEmployeeId - identyfikator
     * @param id - id edytowanego pracownika
     * @return - true, jeśli ktoś ma już taki identyfikator
     */
    boolean existEmployeeWithThisUniqueId(String uniqueEmployeeId, Long id);
}

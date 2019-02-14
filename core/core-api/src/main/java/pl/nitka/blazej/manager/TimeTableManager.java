package pl.nitka.blazej.manager;

import pl.nitka.blazej.enums.EmployeeIdentifiers;
import pl.nitka.blazej.enums.WorkingHolidayDayName;
import pl.nitka.blazej.temporary.FreeDaysForMonth;
import pl.nitka.blazej.temporary.GeneratedMonth;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Manager udostępniający metodę generującą harmonogram.
 *
 * @author Blazej
 */
public interface TimeTableManager {

    /**
     * Metoda generująca harmonogram.
     *
     * @param generatedMonth - wybrany miesiąc
     * @param workingHolidayName - nazwa pracującego dnia weekendowego
     * @param selectedFreeDaysToWork - wybrane święta, jakie mają być pracujące
     * @param employeeIdentifiers - informacja, czy identyfikatorami pracowników mają być ich dane czy ID
     * @param oneChangesOnDay - zmienna informująca, czy jest to grafik jednozmianowy
     * @param startWorkTime - godzina rozpoczęcia pierwszej zmiany
     * @param fileName - nazwa pliku
     * @param generatedDate - data wygenerowania
     * @return - strumień do pliku
     */
    InputStream generateTimeTable(GeneratedMonth generatedMonth, WorkingHolidayDayName workingHolidayName,
                                  List<FreeDaysForMonth> selectedFreeDaysToWork, EmployeeIdentifiers employeeIdentifiers,
                                  boolean oneChangesOnDay, Date startWorkTime, String fileName, Date generatedDate);
}

package pl.nitka.blazej.manager;

import java.util.Date;

/**
 * Manager do zarządzania obiektami urlopów.
 *
 * @author Blazej
 */
public interface VacationManager {

    /**
     * Metoda usuwająca (anulująca) urlop.
     *
     * @param vacationId - id urlopu
     */
    void deleteVacation(long vacationId);

    /**
     * Metoda sprawdzająca, czy inny pracownik nie ma urlopu w danym okresie.
     *
     * @param dateFromVacation - data od urlopu
     * @param dateToVacation   - data do urlopu
     * @param employeeId       - id pracownika, któremu dodawany jest urlop
     * @return - true, jeśli któryś z pracowników ma przynajmniej dzień urlopu w danym zakresie
     */
    boolean otherEmployeeHasVacationInThisScope(Date dateFromVacation, Date dateToVacation, long employeeId);

    /**
     * Metoda zwracająca ilość wykorzystanego urlopu w danym roku przez wskazanego pracownika.
     *
     * @param employeeId - ID pracownika, którego dotyczy sprawdzanie
     * @param year       - rok, na który będzie sprawdzany urlop
     * @return - ilość wykorzystanych dni urlopowych we wskazanym roku
     */
    int getUsedVacationsByYear(long employeeId, int year);

    /**
     * Metoda sprawdzająca, czy jest jeszcze możliwość anulowania urlopu, czyli nie pozostało mniej niż 10 dni
     * do miesiąca, w którym rozpoczyna się urlop.
     *
     * @param vacationId - ID urlopu
     * @return - true, jeśli nie można już anulować urlopu
     */
    boolean vacationIsBlocked(long vacationId);
}

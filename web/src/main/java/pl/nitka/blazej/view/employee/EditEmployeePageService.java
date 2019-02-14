package pl.nitka.blazej.view.employee;

import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import pl.nitka.blazej.entity.employee.Employee;
import pl.nitka.blazej.entity.employee.Vacation;
import pl.nitka.blazej.manager.EmployeeManager;
import pl.nitka.blazej.manager.VacationManager;
import pl.nitka.blazej.utils.DateUtils;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Bean obsługujący ekran edycji danych pracownika (i dodawania urlopu).
 *
 * @author Blazej
 */
@Named
@ViewScoped
public class EditEmployeePageService implements Serializable {

    @EJB
    private EmployeeManager employeeManagerBean;

    @EJB
    private VacationManager vacationManagerBean;

    /**
     * Edytowany pracownik.
     */
    private Employee editedEmployee;

    /**
     * Id pracownika przekazane z listy.
     */
    @Setter
    @Getter
    private long employeeId;

    /**
     * Data od urlopu.
     */
    @Getter
    @Setter
    private Date dateFromVacation;

    /**
     * Data do urlopu.
     */
    @Getter
    @Setter
    private Date dateToVacation;

    /**
     * Informacja, czy włączony jest tryb edycji.
     */
    @Getter
    private boolean editMode;

    /**
     * Informacja, czy inny pracownik ma już urlop w danym zakresie.
     */
    @Getter
    private boolean duplicatedVacation;

    /**
     * Tymczasowa zmienna przechowująca zezwolenie na nadgodziny.
     */
    private boolean tempOvertimeAgreement;

    @PostConstruct
    public void init() {
        editMode = false;
        duplicatedVacation = false;
    }

    /**
     * Metoda zwracająca pracownika (jeśli jest nullem, to pobiera go z bazy po ID).
     *
     * @return - edytowany pracownik
     */
    public Employee getEditedEmployee() {
        if (Objects.isNull(this.editedEmployee)) {
            this.editedEmployee = employeeManagerBean.findEmployee(employeeId);
        }
        return this.editedEmployee;
    }

    /**
     * Liczba dni urlopowych do wykorzystania w aktualnym roku.
     *
     * @return - dni do wykorzystania w obecnym roku
     */
    public int getThisYearAvailableVacation() {
        return employeeManagerBean.calculateThisYearAvailableVacation(employeeId);
    }

    /**
     * Liczba dni urlopowych do wykorzystania w kolejnym roku.
     *
     * @return - dni do wykorzystania w kolejnym roku
     */
    public int getNextYearAvailableVacation() {
        return employeeManagerBean.calculateNextYearAvailableVacation(employeeId);
    }

    /**
     * Liczba dni urlopowych wykorzystanych w aktualnym roku.
     *
     * @return - dni wykorzystane w obecnym roku
     */
    public int getThisYearUsedVacation() {
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        return vacationManagerBean.getUsedVacationsByYear(employeeId, today.get(Calendar.YEAR));
    }

    /**
     * Liczba dni urlopowych wykorzystanych w kolejnym roku.
     *
     * @return - dni wykorzystane w kolejnym roku
     */
    public int getNextYearUsedVacation() {
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        today.add(Calendar.YEAR, 1);
        return vacationManagerBean.getUsedVacationsByYear(employeeId, today.get(Calendar.YEAR));
    }

    /**
     * Włączenie trybu edycji.
     */
    public void turnOnEditMode() {
        editMode = true;
        tempOvertimeAgreement = editedEmployee != null && editedEmployee.getOvertimeAgreement();
    }

    /**
     * Wyłączenie trybu edycji.
     */
    public void turnOffEditMode() {
        editedEmployee.setOvertimeAgreement(tempOvertimeAgreement);
        editMode = false;
    }

    /**
     * Metoda zapisująca zmiany dokonane na obiekcie pracownika.
     */
    public void saveChanges() {
        if (employeeManagerBean.existEmployeeWithThisUniqueId(editedEmployee.getUniqueEmployeeId(), editedEmployee.getId())) {
            String header = "Duplikacja";
            String message = "Uwaga! W systemie znajduje się już użytkownik o takim identyfikatorze!";
            PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage(FacesMessage.SEVERITY_ERROR, header, message));
        } else {
            employeeManagerBean.saveEmployee(editedEmployee);
            editMode = false;
        }
    }

    /**
     * Metoda anulujaca (usuwająca) urlop.
     *
     * @param vacationId - ID urlopu
     */
    public void cancelVacation(long vacationId) {
        vacationManagerBean.deleteVacation(vacationId);
    }

    /**
     * Metoda sprawdzająca, czy któryś z pracowników ma już zarezerwowany przynajmniej jeden dzień urlopowy w tym zakresie.
     */
    public void checkIfOtherEmployeeHasThisVacation() {
        duplicatedVacation = vacationManagerBean.otherEmployeeHasVacationInThisScope(dateFromVacation, dateToVacation, employeeId);
    }

    /**
     * Metoda zapisująca nowy urlop.
     */
    public void addVacation() {
        // Sprawdzenie, czy data do nie jest wcześniejsza od data od
        if (dateFromVacation.compareTo(dateToVacation) > 0) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Data rozpoczęcia urlopu nie może być późniejsza niż data zakończenia", null));
            return;
        }

        // Sprawdzenie, czy lata urlopu są takie same
        Calendar dateFrom = Calendar.getInstance();
        Calendar dateTo = Calendar.getInstance();
        dateFrom.setTime(dateFromVacation);
        dateTo.setTime(dateToVacation);
        if (dateFrom.get(Calendar.YEAR) != dateTo.get(Calendar.YEAR)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Data rozpoczęcia i zakończenia urlopu musi mieścić się w tym samym roku kalendarzowym", null));
            return;
        }

        // Sprawdzenie, czy pracownik nie ma już urlopu w którymś z tych dni
        if (employeeManagerBean.employeeHasVacationInThisScope(dateFromVacation, dateToVacation, employeeId)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pracownik ma już zaplanowany przynajmniej jeden dzień wolny w tym zakresie", null));
            return;
        }

        // Sprawdzenie, czy wyznaczony urlop nie przekracza dozwolonej liczby dni urlopowych w danym roku
        int workingDays = DateUtils.getWorkingDaysNumberFromScope(dateFrom.getTime(), dateTo.getTime());
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        // Ten sam rok, sprawdzamy dla tego roku
        if (today.get(Calendar.YEAR) == dateFrom.get(Calendar.YEAR)) {
            if (workingDays > getThisYearAvailableVacation()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Przekroczono dozwoloną liczbę dni urlopowych na dany rok", null));
                return;
            }
        } else {
            // Przyszły rok
            if (workingDays > getNextYearAvailableVacation()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Przekroczono dozwoloną liczbę dni urlopowych na dany rok", null));
                return;
            }
        }

        Vacation vacation = new Vacation();
        vacation.setDateFromVacation(dateFromVacation);
        vacation.setDateToVacation(dateToVacation);
        vacation.setVacationYear(dateFrom.get(Calendar.YEAR));
        Employee employeeToVacation = employeeManagerBean.findEmployee(employeeId);
        vacation.setEmployee(employeeToVacation);

        // Obliczenie ile dni z podanego zakresu to dni urlopowe, czyli wchodzące w skład liczby dni urlopowych
        int workingVacationDays = DateUtils.getWorkingDaysNumberFromScope(dateFromVacation, dateToVacation);
        vacation.setWorkingDaysInVacation(workingVacationDays);

        // Dodanie urlopu do pracownika
        Employee employee = employeeManagerBean.findEmployee(employeeId);
        employee.addVacation(vacation);
        employeeManagerBean.saveEmployee(employee);

        dateFromVacation = null;
        dateToVacation = null;

        String message = String.format("Pomyślnie dodano urlop, wykorzystane dni urlopowe: %s", workingVacationDays);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    /**
     * Metoda sprawdzająca, czy pracownik uzyskał 26 dni urlopu przed rejestracją.
     *
     * @return - true, jeśli tak
     */
    public boolean fullVacationBeforeEmployment() {
        // Data początku świata, która jest ustawiana w przypadku osiągnięcia 26 dni urlopowych przed rejestracją
        Calendar fullVacationBeforeEmploymentDate = Calendar.getInstance();
        fullVacationBeforeEmploymentDate.setTime(DateUtils.getBeginOfWorldDate());

        // Data, od której pracownik może pobierać 26 dni urlopu
        Calendar fullVacationEmployeeDate = Calendar.getInstance();
        fullVacationEmployeeDate.setTime(employeeManagerBean.findEmployee(employeeId).getFullVacationDate());

        return DateUtils.compareDates(fullVacationBeforeEmploymentDate, fullVacationEmployeeDate);
    }

    /**
     * Metoda sprawdzająca, czy jest jeszcze możliwość anulowania urlopu, czyli nie pozostało mniej niż 10 dni
     * do miesiąca, w którym rozpoczyna się urlop.
     *
     * @param vacationId - ID urlopu
     * @return - true, jeśli nie można już anulować urlopu
     */
    public boolean vacationIsBlocked(long vacationId) {
        return vacationManagerBean.vacationIsBlocked(vacationId);
    }

    /**
     * Metoda zwracająca koniec przyszłego roku jako ostateczną datę możliwą do rezerwacji urlopu.
     *
     * @return - ostatni dzień przyszłego roku
     */
    public Date maxVacationDate() {
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        today.add(Calendar.YEAR, 1);
        today.set(Calendar.MONTH, Calendar.DECEMBER);
        today.set(Calendar.DAY_OF_MONTH, 31);
        return today.getTime();
    }

    /**
     * Metoda zwracająca najbliższą możliwą datę urlopu.
     *
     * @return - najbliższa możliwa data urlopu
     */
    public Date minVacationDate() {
        // Data, która będzie zwrócona (będzie zmieniony jeszcze miesiąc)
        Calendar result = Calendar.getInstance();
        result.setTime(new Date());
        result.set(Calendar.DAY_OF_MONTH, 1);

        // Dzisiejsza data
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        int actualMonth = today.get(Calendar.MONTH);
        today.add(Calendar.DAY_OF_YEAR, 10);

        if (actualMonth == today.get(Calendar.MONTH)) {
            result.set(Calendar.MONTH, actualMonth + 1);
        } else {
            result.set(Calendar.MONTH, actualMonth + 2);
        }

        return result.getTime();
    }

    public List<Vacation> getVacationList() {
        return employeeManagerBean.findEmployee(employeeId).getVacationList();
    }

}

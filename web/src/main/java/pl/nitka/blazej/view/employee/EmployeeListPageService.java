package pl.nitka.blazej.view.employee;

import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import pl.nitka.blazej.entity.employee.Employee;
import pl.nitka.blazej.entity.employee.WorkTime;
import pl.nitka.blazej.manager.EmployeeManager;
import pl.nitka.blazej.providers.SystemParametersProvider;
import pl.nitka.blazej.utils.DateUtils;
import pl.nitka.blazej.enums.CompletedStudies;
import pl.nitka.blazej.temporary.PreviousWorksScope;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Bean widokowy obsługujący ekran z listą pracowników oraz możliwością dodawania pracownika.
 *
 * @author Blazej
 */
@Named
@ViewScoped
public class EmployeeListPageService implements Serializable {

    @EJB
    private EmployeeManager employeeManagerBean;

    @EJB
    private SystemParametersProvider systemParametersProviderBean;

    /**
     * Lista wszystkich pracowników.
     */
    private List<Employee> allEmployeeList;

    /**
     * Lista poprzednich okresów zatrudnienia.
     */
    @Getter
    private List<PreviousWorksScope> previousWorksScopes;

    @Getter
    @Setter
    private Date dateFromWorksScope;

    @Getter
    @Setter
    private Date dateToWorksScope;

    /**
     * Imię.
     */
    @Getter
    @Setter
    private String firstName;

    /**
     * Nazwisko.
     */
    @Getter
    @Setter
    private String lastName;

    /**
     * Data urodzenia
     */
    @Getter
    @Setter
    private Date birthDate;

    /**
     * Data zatrudnienia
     */
    @Getter
    @Setter
    private Date employmentDate;

    /**
     * Ukończona szkoła
     */
    @Getter
    @Setter
    private String completedStudies;

    /**
     * Zgoda na nadgodziny.
     */
    @Getter
    @Setter
    private boolean overtimeAgreement;

    /**
     * Zgłoszenie poprawności wprowadzonych danych.
     */
    @Getter
    @Setter
    private boolean validData;

    @Getter
    @Setter
    private boolean showInputWorksScopesDates;

    /**
     * Inicjalizacja beana.
     */
    @PostConstruct
    public void init() {
        validData = false;
        showInputWorksScopesDates = false;
        previousWorksScopes = new ArrayList<>();
    }

    /**
     * Metoda wywoływana przyciskiem na ekranie, powoduje zapisanie pracownika w bazie.
     */
    public void saveEmployee() {
        if (employmentDate.after(getTodayDate())) {
            String header = "Błędne dane";
            String message = "Uwaga! Data zatrudnienia nie może być późniejsza niż data dzisiejsza!";
            PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage(FacesMessage.SEVERITY_ERROR, header, message));
            return;
        } else if (birthDate.after(getTodayDate()) || birthDate.after(employmentDate)) {
            String header = "Błędne dane";
            String message = "Uwaga! Data urodzenia nie może być późniejsza niż data dzisiejsza oraz data zatrudnienia!";
            PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage(FacesMessage.SEVERITY_ERROR, header, message));
            return;
        }

        // Utworzenie pracownika
        Employee newEmployee = new Employee();
        newEmployee.setPreviousWorkScopes(previousWorksScopes.size());

        // Przepisanie czasu pracy na podstawie ukończonej szkoły
        CompletedStudies completedStudiesFromText = CompletedStudies.getFromString(this.completedStudies);
        WorkTime workTime = DateUtils.createWorkTimeFromSchoolAndWorksScopes(completedStudiesFromText, previousWorksScopes, employmentDate);
        newEmployee.setWorkTime(workTime);
        newEmployee.setFullVacationDate(DateUtils.getFullVacationDate(workTime));

        // Uzupełnienie pozostałych danych
        newEmployee.setFirstName(firstName);
        newEmployee.setLastName(lastName);
        newEmployee.setBirthDate(birthDate);
        newEmployee.setEmploymentDate(employmentDate);
        newEmployee.setOvertimeAgreement(overtimeAgreement);

        // Zapis pracownika
        employeeManagerBean.saveEmployee(newEmployee);

        // Uruchomienie silnika reguł i zapisanie obiektu pracownika
        systemParametersProviderBean.runEmployeeRules(newEmployee);
        employeeManagerBean.saveEmployee(newEmployee);

        // Dodanie pracownika do wyświetlanej listy
        allEmployeeList.add(newEmployee);

        String header = "Sukces";
        String message = "Pracownik został zapisany w systemie";
        PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage(FacesMessage.SEVERITY_INFO, header, message));

        // Reset pól w formularzu
        resetFormAfterAddEmployee();
    }

    /**
     * Metoda zwracająca tekstowe wartości wszystkich rodzajów szkół.
     *
     * @return - lista szkół
     */
    public List<String> getAllCompletedStudiesList() {
        List<String> completedStudiesList = new ArrayList<>();
        Arrays.asList(CompletedStudies.values()).forEach(element -> completedStudiesList.add(element.getSchoolName()));
        return completedStudiesList;
    }

    /**
     * Metoda resetująca wszystkie wartości po zapisie pracownika.
     */
    private void resetFormAfterAddEmployee() {
        this.firstName = null;
        this.lastName = null;
        this.birthDate = null;
        this.employmentDate = null;
        this.completedStudies = null;
        this.overtimeAgreement = false;
        this.validData = false;
        this.previousWorksScopes = new ArrayList<>();
        this.dateToWorksScope = null;
        this.dateFromWorksScope = null;
    }

    /**
     * Metoda usuwająca pracownika po ID.
     *
     * @param employeeId - id usuwanego pracownika
     */
    public void deleteEmployee(long employeeId) {
        employeeManagerBean.deleteEmployee(employeeId);
        this.allEmployeeList = employeeManagerBean.getAll();
    }

    /**
     * Metoda zwracająca listę wszystkich pracowników.
     *
     * @return - lista wszystkich pracowników.
     */
    public List<Employee> getAllEmployees() {
        if (this.allEmployeeList == null) {
            this.allEmployeeList = employeeManagerBean.getAll();
        }
        return this.allEmployeeList;
    }

    /**
     * Metoda zwracająca liczbę pracowników w tabeli.
     *
     * @return - liczba pracowników.
     */
    public int getListSize() {
        return this.allEmployeeList == null ? 0 : this.allEmployeeList.size();
    }

    /**
     * Usuwanie ostatniego okresu pracy.
     */
    public void deleteLastWorksScope() {
        previousWorksScopes.remove(previousWorksScopes.size() - 1);
    }

    /**
     * Metoda zwracająca informację, czy lista jest pusta.
     *
     * @return - true, jeśli pusta
     */
    public boolean isEmptyWorksScopeList() {
        return previousWorksScopes.isEmpty();
    }

    /**
     * Włączenie widoczności panelu z dodawaniem nowego zakresu pracy.
     */
    public void setVisibilityInputWorksScopesDates() {
        showInputWorksScopesDates = true;
        dateFromWorksScope = null;
        dateToWorksScope = null;
    }

    /**
     * Dodanie kolejnego okresu pracy.
     */
    public void saveNewWorksScope() {
        previousWorksScopes.add(new PreviousWorksScope(dateFromWorksScope, dateToWorksScope));
        showInputWorksScopesDates = false;
        dateToWorksScope = null;
        dateFromWorksScope = null;
    }

    /**
     * Wyłączenie widoczności panelu z dodawaniem nowego zakresu pracy.
     */
    public void disableInputWorksScopesDates() {
        showInputWorksScopesDates = false;
        dateToWorksScope = null;
        dateFromWorksScope = null;
    }

    /**
     * Metoda zwracająca dostępny urlop dla danego pracownika.
     *
     * @param employeeId - ID pracownika
     * @return - dni urlopu do wykorzystania w bieżacym roku
     */
    public int calculateAvailableVacation(long employeeId) {
        return employeeManagerBean.calculateThisYearAvailableVacation(employeeId);
    }

    /**
     * Metoda zwracająca aktualny rok kalendarzowy.
     *
     * @return - aktualny rok
     */
    public int getActualYear() {
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        return today.get(Calendar.YEAR);
    }

    public Date getTodayDate() {
        return new Date();
    }
}

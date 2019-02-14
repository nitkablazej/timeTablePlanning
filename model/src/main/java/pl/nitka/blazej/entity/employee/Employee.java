package pl.nitka.blazej.entity.employee;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Encja reprezenująca pracownika.
 *
 * @author Blazej
 */
@Entity
@Getter
@Setter
@NamedQuery(name = "Employee.getAll", query = "SELECT em FROM Employee em")
@NamedQuery(name = "Employee.findById", query = "SELECT em FROM Employee em WHERE em.id = :employeeId")
@NamedQuery(name = "Employee.findByuniqueEmployeeId", query = "SELECT em FROM Employee em WHERE em.id <> :id AND em.uniqueEmployeeId = :uniqueEmployeeId")
@Table(name = "EMPLOYEE")
public class Employee implements Serializable {

    private static final long serialVersionUID = -687991492884005033L;

    /**
     * ID bazodanowe.
     */
    @Id
    @Setter(AccessLevel.NONE)
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Staż pracy (przed rejestracją).
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "WORK_TIME_ID")
    private WorkTime workTime;

    /**
     * Unikatowy identyfikator pracownika.
     */
    @Column(name = "UNIQUE_EMPLOYEE_ID", length = 100, unique = true)
    private String uniqueEmployeeId;

    /**
     * Imię pracownika.
     */
    @Column(name = "FIRST_NAME", length = 100, nullable = false)
    private String firstName;

    /**
     * Nazwisko pracownika.
     */
    @Column(name = "LAST_NAME", length = 100, nullable = false)
    private String lastName;

    /**
     * Data urodzenia.
     */
    @Column(name = "BIRTH_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date birthDate;

    /**
     * Data, od której pracownikowi będą przysługiwały 26 dni urlopowe.
     */
    @Column(name = "FULL_VACATION_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fullVacationDate;

    /**
     * Zbiór urlopów.
     */
    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER, mappedBy = "employee", orphanRemoval = true)
    private List<Vacation> vacationList;

    /**
     * Data zatrudnienia.
     */
    @Column(name = "EMPLOYMENT_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date employmentDate;

    /**
     * Zgoda na nadgodziny.
     */
    @Column(name = "OVERTIME_AGREEMENT", nullable = false)
    private Boolean overtimeAgreement;

    /**
     * Pierwsza praca.
     */
    @Column(name = "FIRST_WORK")
    private Boolean firstWork;

    /**
     * Lista ostatnich wyjść z pracy grupowana po miesiącach.
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "workingEmployee", fetch = FetchType.LAZY)
    private List<LastExitFromWork> lastExitFromWork;

    /**
     * Zmienna niezapisywana w bazie, informuje o ilości poprzednich zatrudnień, do zaprezentowania JBoss Drools.
     */
    @Transient
    private int previousWorkScopes;

    public Employee() {
        this.vacationList = new ArrayList<>();
        this.lastExitFromWork = new ArrayList<>();
    }

    /**
     * Obliczenie tekstowej wartości dni spędzonych w firmie przez pracownika (wyświetlana w tabeli).
     *
     * @return - czas spędzony w firmie przez pracownika
     */
    public String getTimeInCompany() {
        Date today = new Date();
        long employmentMilliseconds = today.getTime() - employmentDate.getTime();
        long daysInCompany = employmentMilliseconds / 86400000;
        String result;
        if (daysInCompany <= 0) {
            result = "0 dni";
        } else if (daysInCompany == 1) {
            result = "1 dzień";
        } else {
            result = String.format("%s dni", daysInCompany);
        }

        return result;
    }

    /**
     * Pobranie posortowanej listy urlopów.
     *
     * @return - posortowana lista urlopów
     */
    public List<Vacation> getVacationList() {
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        int thisYear = today.get(Calendar.YEAR);
        int nextYear = thisYear + 1;

        return vacationList.stream()
                .filter(vacation -> vacation.getVacationYear() == thisYear || vacation.getVacationYear() == nextYear)
                .sorted(Comparator.comparing(Vacation::getDateFromVacation))
                .collect(Collectors.toList());
    }

    /**
     * Dodanie urlopu do listy.
     *
     * @param vacation - urlop
     */
    public void addVacation(Vacation vacation) {
        this.vacationList.add(vacation);
    }
}

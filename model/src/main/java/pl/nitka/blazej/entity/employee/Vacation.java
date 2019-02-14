package pl.nitka.blazej.entity.employee;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * Klasa odzwierciedlająca zaplanowane przez pracownika okresy urlopu.
 *
 * @author Blazej
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "Vacation.getAllWithoutEmployee", query = "SELECT v FROM Vacation v WHERE v.employee.id <> :employeeId")
@NamedQuery(name = "Vacation.getAllForEmployeeOnYear", query = "SELECT v FROM Vacation v WHERE v.employee.id = :employeeId AND v.vacationYear = :vacationYear")
@NamedQuery(name = "Vacation.findById", query = "SELECT v FROM Vacation v WHERE v.id = :vacationId")
@Table(name = "VACATION")
public class Vacation implements Serializable {

    private static final long serialVersionUID = -687991599634005033L;

    /**
     * ID bazodanowe.
     */
    @Id
    @Setter(AccessLevel.NONE)
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Data rozpoczęcia urlopu.
     */
    @Column(name = "DATE_FROM_VACATION", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateFromVacation;

    /**
     * Data zakończenia urlopu.
     */
    @Column(name = "DATE_TO_VACATION", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateToVacation;

    /**
     * Dni pracujące, które wchodzą w czas urlopu.
     */
    @Column(name = "WORKING_DAYS_IN_VACATION", nullable = false)
    private Integer workingDaysInVacation;

    /**
     * Rok urlopu.
     */
    @Column(name = "VACATION_YEAR", nullable = false)
    private Integer vacationYear;

    /**
     * Użytkownik, do którego jest przypisany urlop.
     */
    @ManyToOne
    @JoinColumn(name="EMPLOYEE_ID", nullable = false)
    private Employee employee;
}

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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * Klasa odzwierciedlająca ostatnie wyjścia z pracy pogrupowane po miesiącach.
 *
 * @author Blazej
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "LAST_EXIT_FROM_WORK")
public class LastExitFromWork implements Serializable {

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
     * Miesiąc i rok.
     */
    @Column(name = "MONTH_AND_YEAR", nullable = false, length = 7)
    private String monthAndYear;

    /**
     * Data ostatniego wyjścia z pracy.
     */
    @Column(name = "LAST_EXIT", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastExit;

    /**
     * Użytkownik, do którego jest przypisane wyjście.
     */
    @ManyToOne
    @JoinColumn(name="WORKING_EMPLOYEE_ID", nullable = false)
    private Employee workingEmployee;

}

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
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Klasa odzwierciedlająca staż pracy pracownika (przed rejestracją w systemie).
 *
 * @author Blazej
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "WORK_TIME")
public class WorkTime implements Serializable {

    private static final long serialVersionUID = -687991582884005033L;

    /**
     * ID bazodanowe.
     */
    @Id
    @Setter(AccessLevel.NONE)
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Lata pracy.
     */
    @Column(name = "YEARS", nullable = false)
    private Integer years;

    /**
     * Miesiące pracy.
     */
    @Column(name = "MONTHS", nullable = false)
    private Integer months;

    /**
     * Dni pracy.
     */
    @Column(name = "DAYS", nullable = false)
    private Integer days;

    public WorkTime(Integer years, Integer months, Integer days) {
        this.years = years;
        this.months = months;
        this.days = days;
    }

    /**
     * Metoda dodająca lata (na podstawie szkoły) do przepracowanego okresu
     *
     * @param years - lata na podstawie szkoły
     */
    public void addYears(int years) {
        this.years += years;
    }

    /**
     * Metoda zwracająca tekstową postać okresu przepracowanego przed podjęciem pracy.
     *
     * @return - tekstowy okres przed podjęciem pracy.
     */
    public String getTextWorkedTimeBeforeEmployment() {
        String result = "";

        if (this.years != 0) {
            if (this.years == 1) {
                result = result + this.years + " rok, ";
            } else if (this.years < 5) {
                result = result + this.years + " lata, ";
            } else {
                result = result + this.years + " lat, ";
            }
        }

        if (this.months == 1) {
            result = result + this.months + " miesiąc, ";
        } else if (this.months > 1 && this.months < 5) {
            result = result + this.months + " miesiące, ";
        } else {
            result = result + this.months + " miesięcy, ";
        }

        if (this.days == 1) {
            result = result + this.days + " dzień";
        } else {
            result = result + this.days + " dni";
        }

        return result;
    }
}

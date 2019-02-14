package pl.nitka.blazej.temporary;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Klasa POJO reprezentująca święto, które domyślnie jest wolne w danym roku.
 *
 * @author Blazej
 */
@Getter
@Setter
public class FreeDaysForMonth implements Serializable {

    private static final long serialVersionUID = -6879634928840050585L;

    /**
     * Miesiąc święta.
     */
    private int month;

    /**
     * Dzień święta.
     */
    private int day;

    /**
     * Etykieta.
     */
    private String label;

    public FreeDaysForMonth(int month, int day, String freeDayName) {
        this.month = month;
        this.day = day;
        this.label = String.format("%s.%s - %s", day, month, freeDayName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FreeDaysForMonth that = (FreeDaysForMonth) o;
        return month == that.month &&
                day == that.day &&
                label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(month, day, label);
    }
}

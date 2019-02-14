package pl.nitka.blazej.temporary;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Klasa POJO reprezentująca wybrany miesiąc do generowania grafiku.
 *
 * @author Blazej
 */
@Getter
@Setter
public class GeneratedMonth implements Serializable {

    private static final long serialVersionUID = -687963492884005033L;

    /**
     * Wybrany miesiąc.
     */
    private int monthNumber;

    /**
     * Wybrany rok.
     */
    private int year;

    /**
     * Etykieta.
     */
    private String label;

    /**
     * Nazwa miesiąca.
     */
    private String monthTranslate;

    public GeneratedMonth(int monthNumber, int year, String monthTranslate) {
        this.monthNumber = monthNumber;
        this.year = year;
        this.monthTranslate = monthTranslate;
        this.label = String.format("%s (%s)", monthTranslate, year);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneratedMonth that = (GeneratedMonth) o;
        return monthNumber == that.monthNumber &&
                year == that.year &&
                label.equals(that.label);
    }

    @Override
    public int hashCode() {

        return Objects.hash(monthNumber, year, label);
    }
}

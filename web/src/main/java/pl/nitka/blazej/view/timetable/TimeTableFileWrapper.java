package pl.nitka.blazej.view.timetable;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Klasa będąca wrapperem dla obiektu wygenerowanego harmonogramu (aby na GUI nie były dociągane obiekty z plikami).
 *
 * @author Blazej
 */
@Getter
@Setter
public class TimeTableFileWrapper implements Serializable {

    /**
     * Nazwa pliku.
     */
    private String name;

    /**
     * Data wygenerowania.
     */
    private Date generatedDate;

    /**
     * Identyfikator bazodanowy.
     */
    private long id;

    /**
     * Tryb jednozmianowy.
     */
    private boolean oneChanges;

    public TimeTableFileWrapper(String name, Date generatedDate, long id, boolean oneChanges) {
        this.name = name;
        this.generatedDate = generatedDate;
        this.id = id;
        this.oneChanges = oneChanges;
    }
}

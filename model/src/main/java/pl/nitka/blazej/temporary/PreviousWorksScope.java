package pl.nitka.blazej.temporary;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Klasa POJO odwzorowująca okres poprzedniego zatrudnienia.
 *
 * @author Blazej
 */
@Getter
@Setter
public class PreviousWorksScope implements Serializable {

    private static final long serialVersionUID = 127991492884005033L;

    /**
     * Data od zatrudnienia.
     */
    private Date dateFrom;

    /**
     * Data do zatrudnienia.
     */
    private Date dateTo;

    public PreviousWorksScope(Date dateFrom, Date dateTo) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }
}

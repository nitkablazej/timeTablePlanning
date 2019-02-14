package pl.nitka.blazej.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Getter
@Setter
@NamedQuery(name = "TimeTableFile.getAll", query = "SELECT ttf FROM TimeTableFile ttf")
@Table(name = "TIMETABLE_FILE")
public class TimeTableFile {

    /**
     * ID bazodanowe.
     */
    @Id
    @Setter(AccessLevel.NONE)
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Nazwa pliku.
     */
    @Column(name = "FILE_NAME", length = 100, nullable = false)
    private String fileName;

    /**
     * True, jeśli jedna zmiana, false jeśli dwie
     */
    @Column(name = "ONE_CHANGES", nullable = false)
    private Boolean oneChanges;

    /**
     * Data wygenerowania.
     */
    @Column(name = "GENERATE_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date generateDate;

    /**
     * Zawartość pliku.
     */
    @Column(name = "FILE_CONTENT", nullable = false)
    @Lob
    private byte[] fileContent;

}

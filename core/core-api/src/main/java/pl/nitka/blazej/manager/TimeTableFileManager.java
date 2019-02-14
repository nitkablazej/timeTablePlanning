package pl.nitka.blazej.manager;

import pl.nitka.blazej.entity.TimeTableFile;

import java.util.List;

/**
 * Interfejs managera zarządzającego wygenerowanymi harmonogramami.
 *
 * @author Blazej
 */
public interface TimeTableFileManager {

    /**
     * Metoda zapisująca harmonogram w bazie.
     *
     * @param timeTableFile - obiekt bazodanowy harmonogramu.
     */
    void saveTimeTable(TimeTableFile timeTableFile);

    /**
     * Metoda wyszukująca harmonogram z bazy.
     *
     * @param id - id harmonogramu
     * @return - harmonogram
     */
    TimeTableFile find(long id);

    /**
     * Metoda zwracająca listę wszystkich harmonogramów z bazy.
     *
     * @return - wszystkie harmonogramy.
     */
    List<TimeTableFile> getAll();

    /**
     * Metoda usuwająca harmonogram wg identyfikatora.
     *
     * @param id - identyfikator.
     */
    void deleteFile(long id);
}

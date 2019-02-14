package pl.nitka.blazej.view.timetable;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import pl.nitka.blazej.entity.TimeTableFile;
import pl.nitka.blazej.manager.TimeTableFileManager;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean obsługujący ekran z listą wygenerowanych raportów.
 *
 * @author Blazej
 */
@Named
@ViewScoped
public class TimeTableListPageService implements Serializable {

    @EJB
    private TimeTableFileManager timeTableFileManagerBean;

    /**
     * Lista wszystkich wrapperów harmonogramów.
     */
    private List<TimeTableFileWrapper> allFiles;

    /**
     * Metoda umożliwiająca pobranie harmonogramu.
     *
     * @param id - identyfikator pobieranego harmonogramu
     * @return - plik
     */
    public StreamedContent downloadFile(long id) {
        TimeTableFile file = timeTableFileManagerBean.find(id);
        byte[] content = file.getFileContent();
        return new DefaultStreamedContent(new ByteArrayInputStream(content), "application/vnd.ms-excel", file.getFileName());
    }

    /**
     * Metoda umożliwiająca usunięcie harmonogramu.
     *
     * @param id - identyfikator usuwanego harmonogramu
     */
    public void deleteFile(long id) {
        timeTableFileManagerBean.deleteFile(id);
        List<TimeTableFileWrapper> result = new ArrayList<>();
        timeTableFileManagerBean.getAll().forEach(file -> result.add(new TimeTableFileWrapper(file.getFileName(), file.getGenerateDate(), file.getId(), file.getOneChanges())));
        allFiles = result;
    }

    /**
     * Metoda zwracająca listę wszystkich harmonogramów.
     *
     * @return - lista wszystkich harmonogramów
     */
    public List<TimeTableFileWrapper> getAllFiles() {
        if (allFiles == null) {
            List<TimeTableFileWrapper> result = new ArrayList<>();
            timeTableFileManagerBean.getAll()
                    .forEach(file -> result.add(new TimeTableFileWrapper(file.getFileName(), file.getGenerateDate(), file.getId(), file.getOneChanges())));
            allFiles = result;
        }
        return allFiles;
    }
}

package pl.nitka.blazej.manager;

import pl.nitka.blazej.entity.TimeTableFile;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Bean z logiką zarządzającą wygenerowanymi harmonogramami.
 *
 * @author Blazej
 */
@Stateless
public class TimeTableFileManagerBean implements TimeTableFileManager {

    @PersistenceContext(unitName = "PROJECT_PU")
    private EntityManager entityManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveTimeTable(TimeTableFile timeTableFile) {
        entityManager.persist(timeTableFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeTableFile find(long id) {
        return entityManager.find(TimeTableFile.class, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TimeTableFile> getAll() {
        return entityManager.createNamedQuery("TimeTableFile.getAll").getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFile(long id) {
        entityManager.remove(entityManager.find(TimeTableFile.class, id));
    }
}

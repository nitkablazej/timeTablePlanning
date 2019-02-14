package pl.nitka.blazej.manager;

import pl.nitka.blazej.entity.employee.LastExitFromWork;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Objects;

/**
 * Manager zarządzania encjami informującymi o ostatnim wyjściu z pracy w danym miesiącu.
 *
 * @author Blazej
 */
@Stateless
public class LastExitFromWorkManagerBean {

    @PersistenceContext(unitName = "PROJECT_PU")
    private EntityManager entityManager;

    /**
     * Metoda zapisująca encję do bazy.
     *
     * @param lastExitFromWork - ostatnie wyjście z pracy w danym miesiącu
     */
    public void saveEntity(LastExitFromWork lastExitFromWork) {
        if (Objects.isNull(lastExitFromWork.getId())) {
            entityManager.persist(lastExitFromWork);
        } else {
            entityManager.merge(lastExitFromWork);
        }
    }

}

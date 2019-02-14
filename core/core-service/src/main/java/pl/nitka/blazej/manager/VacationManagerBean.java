package pl.nitka.blazej.manager;

import org.apache.log4j.Logger;
import pl.nitka.blazej.entity.employee.Vacation;
import pl.nitka.blazej.utils.DateUtils;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Manager zarządzania urlopami.
 *
 * @author Blazej
 */
@Stateless
public class VacationManagerBean implements VacationManager {

    private static final Logger LOGGER = Logger.getLogger(VacationManagerBean.class);

    @PersistenceContext(unitName = "PROJECT_PU")
    private EntityManager entityManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteVacation(long vacationId) {
        entityManager.remove(entityManager.find(Vacation.class, vacationId));
        LOGGER.info(String.format("Successful delete vacation with id: %s", vacationId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean otherEmployeeHasVacationInThisScope(Date dateFromVacation, Date dateToVacation, long employeeId) {
        // Wszystkie zaplanowane urlopy (będą sprawdzane tylko urlopy innych pracowników)
        List<Vacation> vacationWithoutEmployee = entityManager.createNamedQuery("Vacation.getAllWithoutEmployee").setParameter("employeeId", employeeId).getResultList();

        // Lista dni, które wchodzą w zakres wskazanego na GUI urlopu
        List<Calendar> vacationDates = DateUtils.getDaysFromScope(dateFromVacation, dateToVacation);

        return DateUtils.isScopesOverlap(vacationDates, vacationWithoutEmployee);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUsedVacationsByYear(long employeeId, int year) {
        List<Vacation> vacationForEmployeeOnYear = entityManager.createNamedQuery("Vacation.getAllForEmployeeOnYear")
                .setParameter("employeeId", employeeId)
                .setParameter("vacationYear", year)
                .getResultList();
        return vacationForEmployeeOnYear.stream().mapToInt(Vacation::getWorkingDaysInVacation).sum();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean vacationIsBlocked(long vacationId) {
        Vacation vacation = findVacation(vacationId);

        // Miesiąc rozpoczęcia urlopu z wyzerowanymi dniami
        Calendar vacationStartDate = Calendar.getInstance();

        if (Objects.nonNull(vacation)) {
            vacationStartDate.setTime(vacation.getDateFromVacation());
            vacationStartDate.set(Calendar.DAY_OF_MONTH, 0);

            // Odjęcie 10 dni (urlop można anulować najpóźniej 10 dni przed miesiącem, w którym się rozpoczyna)
            vacationStartDate.add(Calendar.DAY_OF_YEAR, -10);

            // Sprawdzenie, czy aktualna data nie jest za blisko miesiąca, w którym rozpoczyna się urlop
            Calendar today = Calendar.getInstance();
            today.setTime(new Date());

            return today.after(vacationStartDate);
        }

        return false;
    }

    private Vacation findVacation(long vacationId) {
        try {
            return (Vacation) entityManager.createNamedQuery("Vacation.findById").setParameter("vacationId", vacationId).getSingleResult();
        } catch (NoResultException ex) {
            LOGGER.error(String.format("No vacation with id: %s", vacationId), ex);
        }
        return null;
    }
}

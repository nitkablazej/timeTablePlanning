package pl.nitka.blazej.manager;

import org.apache.log4j.Logger;
import pl.nitka.blazej.entity.employee.Employee;
import pl.nitka.blazej.entity.employee.Vacation;
import pl.nitka.blazej.utils.DateUtils;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Bean z logiką zarządzania pracownikani.
 *
 * @author Blazej
 */
@Stateless
public class EmployeeManagerBean implements EmployeeManager {

    private static final Logger LOGGER = Logger.getLogger(EmployeeManagerBean.class);

    @EJB
    private VacationManager vacationManagerBean;

    @PersistenceContext(unitName = "PROJECT_PU")
    private EntityManager entityManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveEmployee(Employee employee) {
        if (Objects.isNull(employee.getId())) {
            entityManager.persist(employee);
        } else {
            entityManager.merge(employee);
            LOGGER.info(String.format("Successful save employee with id: %s", employee.getId()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Employee> getAll() {
        return entityManager.createNamedQuery("Employee.getAll").getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Employee findEmployee(long employeeId) {
        try {
            return (Employee) entityManager.createNamedQuery("Employee.findById").setParameter("employeeId", employeeId).getSingleResult();
        } catch (NoResultException ex) {
            LOGGER.error(String.format("No editedEmployee with id: %s", employeeId), ex);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteEmployee(long employeeId) {
        entityManager.remove(entityManager.createNamedQuery("Employee.findById").setParameter("employeeId", employeeId).getSingleResult());
        LOGGER.info(String.format("Successful delete editedEmployee with id: %s", employeeId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int calculateThisYearAvailableVacation(long employeeId) {
        Employee employee = findEmployee(employeeId);

        // Data zatrudnienia
        Calendar employmentDate = Calendar.getInstance();
        employmentDate.setTime(employee.getEmploymentDate());

        // Dzisiejsza data
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());

        // Przysługujący urlop
        int availableVacation;

        // Obliczenie przysługujących dni urlopu z uwzględnieniem daty, od której przysługuje 26 dni
        Calendar fullVacationDate = Calendar.getInstance();
        fullVacationDate.setTime(employee.getFullVacationDate());

        // Rok przysługiwania 26 dni urlopu jest wcześniejszy niż obecny, dlatego pracownikowi przysługuje 26 dni
        if (fullVacationDate.get(Calendar.YEAR) < today.get(Calendar.YEAR)) {

            // Pracownik zatrudniony w obecnym roku, dlatego należy obliczyć przysługujący urlop do końca roku
            if (today.get(Calendar.YEAR) == employmentDate.get(Calendar.YEAR)) {
                availableVacation = calculateAvailableVacationDays(today, employmentDate, DateUtils.FULL_VACATION);
            } else {
                // Rok zatrudnienia nie jest rokiem aktualnym, więc przysługuje pełna pula 26 dni
                availableVacation = DateUtils.FULL_VACATION;
            }

        // Rok przysługiwania pełnej puli urlopowej jest w przyszłości, dlatego teraz przysługuje 20 dni
        } else if (fullVacationDate.get(Calendar.YEAR) > today.get(Calendar.YEAR)) {

            // Pracownik zatrudniony w obecnym roku, dlatego należy obliczyć przysługujący urlop do końca roku
            if (today.get(Calendar.YEAR) == employmentDate.get(Calendar.YEAR)) {
                availableVacation = calculateAvailableVacationDays(today, employmentDate, DateUtils.NOT_FULL_VACATION);
            } else {
                // Rok zatrudnienia nie jest rokiem aktualnym, więc przysługuje pełna pula 20 dni
                availableVacation = DateUtils.NOT_FULL_VACATION;
            }

        // Rok przyznania puli 26 dni urlopowych jest aktualnym rokiem, dlatego należy przeliczyć liczbę dni urlopowych
        // z puli 20 dni i liczbę z puli 26 dni
        } else {
            // Jeśli data zatrudnienia była przed datą osiągnięcia 26 dni, to trzeba policzyć te 2 okresy
            if (employee.getEmploymentDate().before(employee.getFullVacationDate())) {

                // Obliczenie od daty zatrudnienia do daty osiągnięcia 26 dni urlopowych
                long daysToEndOfYear = DateUtils.getDifferentBetweenDates(employmentDate.getTime(), fullVacationDate.getTime());

                // Pomnożenie dni urlopowych z całego roku * procent dni z roku, jaki pozostał do końca
                // Jeśli zostanie reszta po dzieleniu, do dodawany jest kolejny dzień (na korzyść pracownika)
                double temp = DateUtils.NOT_FULL_VACATION * (daysToEndOfYear * 1.0 / DateUtils.DAYS_IN_YEAR);
                int beforeFullVacationDate = (int) temp;
                if (temp - beforeFullVacationDate > 0) {
                    beforeFullVacationDate++;
                }

                // Obliczenie dni urlopowych od daty osiągnięcia 26 dni urlopowych do końca roku
                int afterFullVacationDate = calculateAvailableVacationDays(today, fullVacationDate, DateUtils.FULL_VACATION);;

                availableVacation = beforeFullVacationDate + afterFullVacationDate;
            } else {
                // Data zatrudnienia była już w okresie 26 dni urlopowych, dlatego wystarczy policzyć proporcjonalnie ilość dni
                availableVacation = calculateAvailableVacationDays(today, employmentDate, DateUtils.FULL_VACATION);
            }
        }

        int usedVacationInThisYear = vacationManagerBean.getUsedVacationsByYear(employeeId, today.get(Calendar.YEAR));
        return availableVacation - usedVacationInThisYear;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int calculateNextYearAvailableVacation(long employeeId) {
        Employee employee = findEmployee(employeeId);

        // Dzisiejsza data + 1 rok
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        today.add(Calendar.YEAR, 1);

        // Data przysługiwania 26 dni urlopowych
        Calendar fullVacationDate = Calendar.getInstance();
        fullVacationDate.setTime(employee.getFullVacationDate());

        // Przysługujący urlop
        int availableVacation;

        // W kolejnym roku nie będzie jeszcze przysługiwał 26 dniowy urlop
        if (fullVacationDate.get(Calendar.YEAR) > today.get(Calendar.YEAR)) {
            availableVacation = DateUtils.NOT_FULL_VACATION;
        } else if (fullVacationDate.get(Calendar.YEAR) < today.get(Calendar.YEAR)) {
            // W kolejnym roku będzie już przysługiwał 26 dniowy urlop
            availableVacation = DateUtils.FULL_VACATION;
        } else {
            int daysBeforeFullVacationDate = fullVacationDate.get(Calendar.DAY_OF_YEAR);
            int daysAfterFullVacationDate = DateUtils.DAYS_IN_YEAR - daysBeforeFullVacationDate;

            // Obliczenie, ile urlopu przysługiwało do daty osiągnięcia 26 dni
            double temp = DateUtils.NOT_FULL_VACATION * (daysBeforeFullVacationDate * 1.0 / DateUtils.DAYS_IN_YEAR);
            int vacationBeforeFullVacationDate = (int) temp;
            if (temp - vacationBeforeFullVacationDate > 0) {
                ++vacationBeforeFullVacationDate;
            }

            // Obliczenie, ile urlopu przysługiwało od daty osiągnięcia 26 dni
            temp = DateUtils.FULL_VACATION * (daysAfterFullVacationDate * 1.0 / DateUtils.DAYS_IN_YEAR);
            int vacationAfterFullVacationDate = (int) temp;
            if (temp - vacationAfterFullVacationDate > 0) {
                ++vacationAfterFullVacationDate;
            }

            availableVacation = vacationBeforeFullVacationDate + vacationAfterFullVacationDate;
        }

        int usedVacationInNextYear = vacationManagerBean.getUsedVacationsByYear(employeeId, today.get(Calendar.YEAR));
        return availableVacation - usedVacationInNextYear;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean employeeHasVacationInThisScope(Date dateFromVacation, Date dateToVacation, long employeeId) {
        // Lista dni z wybranego zakresu urlopu
        List<Calendar> vacationFromSelectedScope = DateUtils.getDaysFromScope(dateFromVacation, dateToVacation);

        // Lista urlopów edytowanego pracownika
        List<Vacation> editedEmployeeVacations;
        editedEmployeeVacations = findEmployee(employeeId).getVacationList();
        return DateUtils.isScopesOverlap(vacationFromSelectedScope, editedEmployeeVacations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existEmployeeWithThisUniqueId(String uniqueEmployeeId, Long id) {
        return entityManager.createNamedQuery("Employee.findByuniqueEmployeeId")
                .setParameter("uniqueEmployeeId", uniqueEmployeeId)
                .setParameter("id", id)
                .getResultList().size() != 0;
    }

    /**
     * Metoda obliczająca propocjonalnie ile dni urlopu do końca roku może wykorzystać pracownik (od daty zatrudnienia).
     *
     * @param today - dzisiejsza data
     * @param employmentDate - data zatrudnienia
     * @param allVacationsDays - pełna pula dni urlopowych
     * @return - liczba urlopowych do końca roku
     */
    private int calculateAvailableVacationDays(Calendar today, Calendar employmentDate, int allVacationsDays) {
        int result;
        // Obliczenie różnicy dni od daty zatrudnienia do końca roku
        Calendar endOfYear = Calendar.getInstance();
        endOfYear.setTime(today.getTime());
        endOfYear.set(Calendar.MONTH, Calendar.DECEMBER);
        endOfYear.set(Calendar.DAY_OF_MONTH, 31);
        long daysToEndOfYear = DateUtils.getDifferentBetweenDates(employmentDate.getTime(), endOfYear.getTime());

        // Pomnożenie dni urlopowych z całego roku * procent dni z roku, jaki pozostał do końca
        // Jeśli zostanie reszta po dzieleniu, do dodawany jest kolejny dzień (na korzyść pracownika)
        double temp = allVacationsDays * (daysToEndOfYear * 1.0 / DateUtils.DAYS_IN_YEAR);
        result = (int) temp;
        if (temp - result > 0) {
            result++;
        }
        return result;
    }

}
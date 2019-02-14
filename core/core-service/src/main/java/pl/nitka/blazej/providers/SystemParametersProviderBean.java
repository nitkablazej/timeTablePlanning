package pl.nitka.blazej.providers;

import org.apache.log4j.Logger;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import pl.nitka.blazej.entity.employee.Employee;
import pl.nitka.blazej.manager.EmployeeManager;
import pl.nitka.blazej.manager.SystemParametersManagerBean;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.Map;

/**
 * Singleton przechowujący listę wszystkich parametrów z bazy.
 *
 * @author Blazej
 */
@Singleton
public class SystemParametersProviderBean implements SystemParametersProvider {

    private static final Logger LOGGER = Logger.getLogger(SystemParametersProviderBean.class);

    @Inject
    private SystemParametersManagerBean systemParametersManagerBean;

    @Inject
    private EmployeeManager employeeManagerBean;

    /**
     * Mapa parametrów systemowych.
     */
    private Map<String, String> parametersMap;

    private KieSession kieSession;

    @PostConstruct
    public void initializeSingleton() {
        parametersMap = systemParametersManagerBean.getParametersMap();
        LOGGER.info(String.format("Successful initialize parameters map with %s params", parametersMap.size()));

        KieServices kieServices = KieServices.Factory.get();
        KieContainer kieContainer = kieServices.getKieClasspathContainer();
        kieSession = kieContainer.newKieSession("timeTablePlanningRules-session");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runEmployeeRules(Employee newEmployee) {
        // Pobranie prefixu do identyfikatora
        String companyPrefix = getParameter("COMPANY_PREFIX");

        kieSession.setGlobal("companyPrefix", companyPrefix);
        kieSession.setGlobal("logger", LOGGER);
        kieSession.insert(newEmployee);
        kieSession.fireAllRules();

        // Jeśli taki ID już istnieje, to zostanie dopisany id na koniec tekstu
        if (employeeManagerBean.existEmployeeWithThisUniqueId(newEmployee.getUniqueEmployeeId(), newEmployee.getId())) {
            String uniqueId = newEmployee.getUniqueEmployeeId();
            newEmployee.setUniqueEmployeeId(String.format("%s_%s", uniqueId, newEmployee.getId()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParameter(String paramName) {
        return parametersMap.get(paramName);
    }
}

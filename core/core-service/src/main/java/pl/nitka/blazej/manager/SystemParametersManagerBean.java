package pl.nitka.blazej.manager;

import pl.nitka.blazej.entity.SystemParameters;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean z logiką zarządzania parametrami systemowymi.
 *
 * @author Blazej
 */
public class SystemParametersManagerBean {

    @PersistenceContext(unitName = "PROJECT_PU")
    private EntityManager entityManager;

    /**
     * Metoda zwracająca mapę wszystkich parametrów z bazy.
     *
     * @return - mapa wszystkich parametrów
     */
    public Map<String, String> getParametersMap() {
        List<SystemParameters> parametersList = entityManager.createNamedQuery("SystemParameters.getAll").getResultList();
        Map<String, String> resultMap = new HashMap<>();
        parametersList.forEach(parameter -> resultMap.put(parameter.getParamName(), parameter.getParamValue()));
        return resultMap;
    }
}

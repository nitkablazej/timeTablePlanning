package pl.nitka.blazej.providers;

import pl.nitka.blazej.entity.employee.Employee;

/**
 * Interfejs do singletona parametrów systemowych.
 *
 * @author Blazej
 */
public interface SystemParametersProvider {

    /**
     * Metoda zwracająca parametr według jego nazwy.
     *
     * @param paramName - nazwa parametru
     * @return - wartość parametru
     */
    String getParameter(String paramName);

    /**
     * Metoda wywołująca silnik reguł i przypisująca ID pracownikowi.
     *
     * @param newEmployee - dodawany pracownik
     */
    void runEmployeeRules(Employee newEmployee);
}

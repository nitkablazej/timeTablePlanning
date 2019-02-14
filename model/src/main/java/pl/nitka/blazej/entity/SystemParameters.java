package pl.nitka.blazej.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Encja z parametrami systemowymi.
 *
 * @author Blazej
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "SystemParameters.getAll", query = "SELECT sp FROM SystemParameters sp")
@Table(name = "SYSTEM_PARAMS")
public class SystemParameters {

    /**
     * ID bazodanowe.
     */
    @Id
    @Setter(AccessLevel.NONE)
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Nazwa parametru.
     */
    @Column(name = "PARAM_NAME", length = 100, nullable = false)
    private String paramName;

    /**
     * Wartość parametru.
     */
    @Column(name = "PARAM_VALUE", length = 100, nullable = false)
    private String paramValue;
}

package please.change.me.common.mail.testsupport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 採番
 */
@Entity
@Table(name = "ID_GENERATOR")
public class IdGenerator {

    public IdGenerator() {
    }

    public IdGenerator(final String id, final int generatedValue) {
        this.id = id;
        this.generatedValue = generatedValue;
    }

    @Id
    @Column(name = "ID", length = 2, nullable = false)
    public String id;

    @Column(name = "GENERATED_VALUE", length=10, nullable = false)
    public Integer generatedValue;
}

package please.change.me.common.mail.html.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * ID採番
 */
@Entity
@Table(name = "ID_GENERATE_MAIL")
public class IdGenerateMail {

    public IdGenerateMail() {
    }

    public IdGenerateMail(final String id, final Integer no) {
        this.id = id;
        this.no = no;
    }

    @Id
    @Column(name = "ID", length = 2, nullable = false)
    public String id;

    @Column(name = "NO", precision = 10, nullable = false)
    public Integer no;
}

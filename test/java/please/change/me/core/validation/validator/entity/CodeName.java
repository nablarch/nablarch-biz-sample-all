package please.change.me.core.validation.validator.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * コード名
 */
@Entity
@IdClass(CodeName.CodeNameId.class)
@Table(name = "CODE_NAME_TEST")
public class CodeName {

    public CodeName() {
    }

    public CodeName(final String id, final String value, final String lang,
            final Integer sortOrder, final String name) {
        this.id = id;
        this.value = value;
        this.lang = lang;
        this.sortOrder = sortOrder;
        this.name = name;
    }

    @Id
    @Column(name = "ID", length = 6, nullable = false)
    public String id;

    @Id
    @Column(name = "VALUE", length = 2, nullable = false)
    public String value;

    @Id
    @Column(name = "LANG", length = 2, nullable = false)
    public String lang;

    @Column(name = "SORT_ORDER", nullable = false)
    public Integer sortOrder;

    @Column(name = "NAME", length = 50, nullable = false)
    public String name;

    @Column(name = "SHORT_NAME", length = 50)
    public String shortName;

    @Column(name = "OPTION01", length = 50)
    public String option01;

    @Column(name = "OPTION02", length = 50)
    public String option02;

    @Column(name = "OPTION03", length = 50)
    public String option03;

    public static class CodeNameId {

        @Column(name = "ID", length = 6, nullable = false)
        public String id;

        @Column(name = "VALUE", length = 2, nullable = false)
        public String value;

        @Column(name = "LANG", length = 2, nullable = false)
        public String lang;
    }

}

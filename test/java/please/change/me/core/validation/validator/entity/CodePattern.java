package please.change.me.core.validation.validator.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * コードパターン
 */
@Entity
@IdClass(CodePattern.CodePatternId.class)
@Table(name = "CODE_PATTERN_TEST")
public class CodePattern {

    public CodePattern() {
    }

    public CodePattern(final String id, final String value, final String pattern01, final String pattern02,
            final String pattern03) {
        this.id = id;
        this.value = value;
        this.pattern01 = pattern01;
        this.pattern02 = pattern02;
        this.pattern03 = pattern03;
    }

    @Id
    @Column(name = "ID", length = 6, nullable = false)
    public String id;

    @Id
    @Column(name = "VALUE", length = 2, nullable = false)
    public String value;

    @Column(name = "PATTERN01", length = 1, nullable = false)
    public String pattern01;

    @Column(name = "PATTERN02", length = 1)
    public String pattern02;

    @Column(name = "PATTERN03", length = 1)
    public String pattern03;

    public static class CodePatternId {

        @Column(name = "ID", length = 6, nullable = false)
        public String id;

        @Column(name = "VALUE", length = 2, nullable = false)
        public String value;
    }

}

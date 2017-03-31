package please.change.me.core.validation.validator;

/**
 * コード名
 */
public class CodeName {

    public CodeName(final String id, final String value, final String lang,
            final Long sortOrder, final String name) {
        this.id = id;
        this.value = value;
        this.lang = lang;
        this.sortOrder = sortOrder;
        this.name = name;
    }

    public String id;

    public String value;

    public String lang;

    public Long sortOrder;

    public String name;

    public String shortName;

    public String option01;
}

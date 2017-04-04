package please.change.me.core.validation.validator;

/**
 * コードパターン
 */
public class CodePattern {

    public CodePattern(final String id, final String value, final String pattern01, final String pattern02,
            final String pattern03) {
        this.id = id;
        this.value = value;
        this.pattern01 = pattern01;
        this.pattern02 = pattern02;
        this.pattern03 = pattern03;
    }

    public String id;

    public String value;

    public String pattern01;

    public String pattern02;

    public String pattern03;

}

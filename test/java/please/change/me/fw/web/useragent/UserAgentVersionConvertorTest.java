package please.change.me.fw.web.useragent;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * {@link please.change.me.fw.web.useragent.UserAgentVersionConvertor}のテスト
 *
 * @author TIS
 */
public class UserAgentVersionConvertorTest {

    UserAgentVersionConvertor target = new UserAgentVersionConvertor();

    /**
     * 変換バリエーションテスト。
     */
    @Test
    public void testConvert() {

        assertThat(target.convert("")
                , is(""));

        assertThat(target.convert("1.2")
                , is("1.2"));

        target.setPadding("_");
        assertThat(target.convert(""), is("_ __ ___"));
        assertThat(target.convert("1"), is("_1 __ ___"));
        assertThat(target.convert("1.2"), is("_1 __2 ___"));
        assertThat(target.convert("1.2.3"), is("_1 __2 ___3"));
        assertThat(target.convert("1.2.3.4"), is("_1 __2 ___3"));

        assertThat(target.convert("abc"), is("_ __ ___"));
    }

    @Test
    public void testNullValue() {
        assertThat(target.convert(null), is(nullValue()));
    }
}

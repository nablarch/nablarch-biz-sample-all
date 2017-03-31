package please.change.me.fw.web.useragent;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * {@link please.change.me.fw.web.useragent.UserAgentNameConvertor}のテスト
 *
 * @author TIS
 */
public class UserAgentNameConvertorTest {

    private String value = "Abc Def Ghi";

    private UserAgentNameConvertor target = new UserAgentNameConvertor();

    /**
     * 変換バリエーションテスト。
     */
    @Test
    public void testConvert() {


        assertThat(target.convert(null), is(nullValue()));
        assertThat(target.convert(value), is("abc def ghi"));

        assertThat(target.convert(""), is(""));
        assertThat(target.convert(value), is("abc def ghi"));

        target.setToLowerCase(true);
        assertThat(target.convert(value), is("abc def ghi"));

        target.setToLowerCase(false);
        assertThat(target.convert(value), is("Abc Def Ghi"));

        target.setToLowerCase(true);
        target.setReplaceFrom(" ");
        target.setReplaceTo("_");
        assertThat(target.convert(value), is("abc_def_ghi"));

        target.setToLowerCase(false);
        target.setReplaceFrom(" ");
        target.setReplaceTo("_");

        assertThat(target.convert(value), is("Abc_Def_Ghi"));
    }

    @Test(expected = IllegalStateException.class)
    public void testReplaceFromNotSet() {

        target.setReplaceTo("_");
        target.convert(value);
    }

    @Test(expected = IllegalStateException.class)
    public void testReplaceToNotSet() {
        target.setReplaceFrom(" ");
        target.convert(value);
    }

    @Test
    public void testReplaceBothNotSet() {

        assertThat(target.convert(value), is("abc def ghi"));
    }
}

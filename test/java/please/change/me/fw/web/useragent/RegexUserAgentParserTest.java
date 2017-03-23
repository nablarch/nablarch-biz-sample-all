package please.change.me.fw.web.useragent;

import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.fw.web.useragent.UserAgent;
import nablarch.fw.web.useragent.UserAgentParser;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link RegexUserAgentParser}のテストクラス。
 * 
 * @author TIS
 */
public class RegexUserAgentParserTest {


    /**
     * マッピング未定義の場合
     */
    @Test
    public void testNoMapping() {
        String uaText = "Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko";

        RegexUserAgentParser parser = new RegexUserAgentParser();
        parser.setOsSetting(new UserAgentPatternSetting());
        parser.setBrowserSetting(new UserAgentPatternSetting());
        UserAgent userAgent = parser.parse(uaText);
        assertThat(userAgent, is(notNullValue()));
        assertThat(userAgent.getText(), is(uaText));
        assertThat(userAgent.getOsType(), is("UnknownType"));
        assertThat(userAgent.getOsName(), is("UnknownName"));
        assertThat(userAgent.getOsVersion(), is("UnknownVersion"));
        assertThat(userAgent.getBrowserType(), is("UnknownType"));
        assertThat(userAgent.getBrowserName(), is("UnknownName"));
        assertThat(userAgent.getBrowserVersion(), is("UnknownVersion"));
    }

    TypePattern winMacType = new TypePattern();
    TypePattern ffChromeType = new TypePattern();
    TypePattern geckoType = new TypePattern();

    @Before
    public void setUp() {
        winMacType.setPattern("(?i).*(windows|mac os x).*");
        winMacType.setName("os");
        ffChromeType.setPattern("(?i).*(firefox|chrome).*");
        ffChromeType.setName("firefox_chrome");
        geckoType.setPattern("(?i).*Gecko.*");
        geckoType.setName("Gecko");
    }


    /**
     * デフォルト値を設定した場合
     */
    @Test
    public void testSetDefaults() {
        RegexUserAgentParser parser = new RegexUserAgentParser();

        // OS情報
        ItemPattern itemPattern = new ItemPattern();
        itemPattern.setPattern("(?i).*(windows|mac os x)[\\D+]*([\\d\\._]*).*");
        itemPattern.setNameIndex(1);
        itemPattern.setVersionIndex(2);

        // ブラウザ情報
        ItemPattern browserPattern = new ItemPattern();
        browserPattern.setPattern("(?i).*(firefox|chrome)[\\s/]*([\\d\\.]*).*");
        browserPattern.setNameIndex(1);
        browserPattern.setVersionIndex(2);


        UserAgentPatternSetting osSetting = new UserAgentPatternSetting();
        osSetting.addTypePattern(winMacType);
        osSetting.addItemPattern(itemPattern);
        parser.setOsSetting(osSetting);

        UserAgentPatternSetting browserSetting = new UserAgentPatternSetting();
        browserSetting.addTypePattern(ffChromeType);
        browserSetting.addItemPattern(browserPattern);
        parser.setBrowserSetting(browserSetting);

        UserAgent userAgent = parser.parse("Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko");
        assertThat(userAgent, is(notNullValue()));

        // マッチしたものは抽出された値
        assertThat(userAgent.getOsType(), is("os"));
        assertThat(userAgent.getOsName(), is("Windows"));
        assertThat(userAgent.getOsVersion(), is("6.1"));

        userAgent = parser.parse("Mozilla/5.0 (Linux; Android 4.2.2; HTC One Build/JDQ39) AppleWebKit/537.36 (KHTML, " +
                                         "like Gecko) Chrome/30.0.1599.30 Mobile Safari/537.36");
        assertThat(userAgent, is(notNullValue()));

        // マッチしないものはnull
        assertThat(userAgent.getOsType(), is("UnknownType"));
        assertThat(userAgent.getOsName(), is("UnknownName"));
        assertThat(userAgent.getOsVersion(), is("UnknownVersion"));





        userAgent = parser.parse("Mozilla/5.0 (Linux; Android 4.2.2; HTC One Build/JDQ39) " +
                                         "AppleWebKit/537.36 (KHTML, " +
                                         "like Gecko) Chrome/30.0.1599.30 Mobile Safari/537" +
                                         ".36");
        assertThat(userAgent, is(notNullValue()));

        // マッチしたものは抽出された値
        assertThat(userAgent.getBrowserType(), is("firefox_chrome"));
        assertThat(userAgent.getBrowserName(), is("Chrome"));
        assertThat(userAgent.getBrowserVersion(), is("30.0.1599.30"));

        userAgent = parser.parse("Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko");
        assertThat(userAgent, is(notNullValue()));

        // マッチしないものはnull
        assertThat(userAgent.getBrowserType(), is("UnknownType"));
        assertThat(userAgent.getBrowserName(), is("UnknownName"));
        assertThat(userAgent.getBrowserVersion(), is("UnknownVersion"));
    }

    /**
     * パターン未設定の定義を設定した場合
     */
    @Test
    public void testNoPattern() {
        RegexUserAgentParser parser = new RegexUserAgentParser();

        ItemPattern osPattern = new ItemPattern();
        osPattern.setPattern("(?i).*(windows|mac os x)[\\D+]*([\\d\\._]*).*");
        osPattern.setNameIndex(1);
        osPattern.setVersionIndex(2);

        UserAgentPatternSetting osSetting = new UserAgentPatternSetting();
        osSetting.addTypePattern(winMacType);
        osSetting.addItemPattern(osPattern);
        parser.setOsSetting(osSetting);


        ItemPattern browserPattern = new ItemPattern();
        browserPattern.setPattern("(?i).*(msie\\s|trident.+rv:)([\\d\\.]*).*");
        browserPattern.setName("ie");
        browserPattern.setVersionIndex(2);

        UserAgentPatternSetting browserSetting = new UserAgentPatternSetting();
        browserSetting.addTypePattern(geckoType);
        browserSetting.addItemPattern(browserPattern);
        parser.setBrowserSetting(browserSetting);


        UserAgent userAgent = parser.parse("Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko");
        assertThat(userAgent, is(notNullValue()));

        assertThat(userAgent.getOsType(), is("os"));
        assertThat(userAgent.getOsName(), is("Windows"));
        assertThat(userAgent.getOsVersion(), is("6.1"));
        assertThat(userAgent.getBrowserType(), is("Gecko"));
        assertThat(userAgent.getBrowserName(), is("ie"));
        assertThat(userAgent.getBrowserVersion(), is("11.0"));
    }

    /**
     * 項目タイプの指定がない場合
     */
    @Test
    public void testNoTypePatternList() {


        RegexUserAgentParser parser = new RegexUserAgentParser();

        ItemPattern osPattern = new ItemPattern();
        osPattern.setPattern("(?i).*(windows|mac os x)[\\D+]*([\\d\\._]*).*");
        osPattern.setNameIndex(1);
        osPattern.setVersionIndex(2);

        UserAgentPatternSetting osSetting = new UserAgentPatternSetting();
        osSetting.addItemPattern(osPattern);
        parser.setOsSetting(osSetting);


        ItemPattern browserPattern = new ItemPattern();
        browserPattern.setPattern("(?i).*(msie\\s|trident.+rv:)([\\d\\.]*).*");
        browserPattern.setName("ie");
        browserPattern.setVersionIndex(2);

        UserAgentPatternSetting browserSetting = new UserAgentPatternSetting();
        browserSetting.addItemPattern(browserPattern);
        parser.setBrowserSetting(browserSetting);


        UserAgent userAgent = parser.parse("Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko");
        assertThat(userAgent, is(notNullValue()));

        // パターン未設定は無視され、次の定義でマッチ
        assertThat(userAgent.getOsType(), is("UnknownType")); // 項目タイプのみデフォルト
        assertThat(userAgent.getOsName(), is("Windows"));
        assertThat(userAgent.getOsVersion(), is("6.1"));

        assertThat(userAgent.getBrowserType(), is("UnknownType")); // 項目タイプのみデフォルト
        assertThat(userAgent.getBrowserName(), is("ie"));
        assertThat(userAgent.getBrowserVersion(), is("11.0"));
    }

    /**
     * 項目タイプの指定がない場合
     */
    @Test
    public void testNoItemPatternList() {

        RegexUserAgentParser parser = new RegexUserAgentParser();

        UserAgentPatternSetting osSetting = new UserAgentPatternSetting();
        osSetting.addTypePattern(winMacType);
        parser.setOsSetting(osSetting);


        UserAgentPatternSetting browserSetting = new UserAgentPatternSetting();
        browserSetting.addTypePattern(geckoType);
        parser.setBrowserSetting(browserSetting);

        UserAgent userAgent = parser.parse("Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko");
        assertThat(userAgent, is(notNullValue()));

        // パターン未設定は無視され、次の定義でマッチ
        assertThat(userAgent.getOsType(), is("os")); // 項目タイプのみ設定される
        assertThat(userAgent.getOsName(), is("UnknownName"));
        assertThat(userAgent.getOsVersion(), is("UnknownVersion"));

        assertThat(userAgent.getBrowserType(), is("Gecko")); // 項目タイプのみ設定される
        assertThat(userAgent.getBrowserName(), is("UnknownName"));
        assertThat(userAgent.getBrowserVersion(), is("UnknownVersion"));
    }

    /**
     * 項目名称インデックスの指定なし、項目名称の指定ありの場合
     */
    @Test
    public void testNoNameIndex() {
        String uaText = "Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko";

        RegexUserAgentParser parser = new RegexUserAgentParser();

        ItemPattern osPattern = new ItemPattern();
        osPattern.setPattern("(?i).*(windows|mac os x)[\\D+]*([\\d\\._]*).*");
        osPattern.setName("os");
        osPattern.setVersionIndex(2);

        UserAgentPatternSetting osSetting = new UserAgentPatternSetting();
        osSetting.addTypePattern(winMacType);
        osSetting.addItemPattern(osPattern);
        parser.setOsSetting(osSetting);


        ItemPattern browserPattern = new ItemPattern();
        browserPattern.setPattern("(?i).*(msie\\s|trident.+rv:)([\\d\\.]*).*");
        browserPattern.setNameIndex(1);
        browserPattern.setVersionIndex(2);

        UserAgentPatternSetting browserSetting = new UserAgentPatternSetting();
        browserSetting.addTypePattern(geckoType);
        browserSetting.addItemPattern(browserPattern);
        parser.setBrowserSetting(browserSetting);

        UserAgent userAgent = parser.parse(uaText);
        assertThat(userAgent, is(notNullValue()));

        // パターン未設定は無視され、次の定義でマッチ
        assertThat(userAgent.getOsType(), is("os"));
        assertThat(userAgent.getOsName(), is("os"));
        assertThat(userAgent.getOsVersion(), is("6.1"));
        assertThat(userAgent.getBrowserType(), is("Gecko"));
        assertThat(userAgent.getBrowserName(), is("Trident/7.0; rv:"));
        assertThat(userAgent.getBrowserVersion(), is("11.0"));
    }

    /**
     * 項目名称インデックスの指定なし、項目名称の指定なしの場合
     */
    @Test
    public void testNoNameIndexAndType() {
        String uaText = "Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko";

        RegexUserAgentParser parser = new RegexUserAgentParser();

        ItemPattern osPattern = new ItemPattern();
        osPattern.setPattern("(?i).*(windows|mac os x)[\\D+]*([\\d\\._]*).*");
        osPattern.setVersionIndex(2);

        UserAgentPatternSetting osSetting = new UserAgentPatternSetting();
        osSetting.addItemPattern(osPattern);
        parser.setOsSetting(osSetting);


        ItemPattern browserPattern = new ItemPattern();
        browserPattern.setPattern("(?i).*(msie\\s|trident.+rv:)([\\d\\.]*).*");
        browserPattern.setVersionIndex(2);

        UserAgentPatternSetting browserSetting = new UserAgentPatternSetting();
        browserSetting.addItemPattern(browserPattern);
        parser.setBrowserSetting(browserSetting);


        UserAgent userAgent = parser.parse(uaText);
        assertThat(userAgent, is(notNullValue()));

        // パターン未設定は無視され、次の定義でマッチ
        assertThat(userAgent.getOsType(), is("UnknownType"));
        assertThat(userAgent.getOsName(), is("UnknownName")); // 項目名称はデフォルト
        assertThat(userAgent.getOsVersion(), is("6.1"));
        assertThat(userAgent.getBrowserType(), is("UnknownType"));
        assertThat(userAgent.getBrowserName(), is("UnknownName")); // 項目名称はデフォルト
        assertThat(userAgent.getBrowserVersion(), is("11.0"));
    }

    /**
     * バージョンインデックスの指定がない場合
     */
    @Test
    public void testNoVersionIndex() {
        String uaText = "Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko";

        RegexUserAgentParser parser = new RegexUserAgentParser();

        ItemPattern osPattern = new ItemPattern();
        osPattern.setPattern("(?i).*(windows|mac os x)[\\D+]*([\\d\\._]*).*");
        osPattern.setName("os");
        osPattern.setNameIndex(1);

        UserAgentPatternSetting osSetting = new UserAgentPatternSetting();
        osSetting.addTypePattern(winMacType);
        osSetting.addItemPattern(osPattern);

        parser.setOsSetting(osSetting);

        ItemPattern browserPattern = new ItemPattern();
        browserPattern.setPattern("(?i).*(msie\\s|trident.+rv:)([\\d\\.]*).*");
        browserPattern.setName("ie");

        UserAgentPatternSetting browserSetting = new UserAgentPatternSetting();
        browserSetting.addTypePattern(geckoType);
        browserSetting.addItemPattern(browserPattern);
        parser.setBrowserSetting(browserSetting);


        UserAgent userAgent = parser.parse(uaText);
        assertThat(userAgent, is(notNullValue()));

        assertThat(userAgent.getOsType(), is("os"));
        assertThat(userAgent.getOsName(), is("Windows"));
        assertThat(userAgent.getOsVersion(), is("UnknownVersion")); // バージョンのみデフォルト
        assertThat(userAgent.getBrowserType(), is("Gecko"));
        assertThat(userAgent.getBrowserName(), is("ie"));
        assertThat(userAgent.getBrowserVersion(), is("UnknownVersion")); // バージョンのみデフォルト
    }

    /**
     * 不正な正規表現パターンを指定した場合
     */
    @Test
    public void testInvalidPattern() {

        try {
            ItemPattern osPattern = new ItemPattern();
            osPattern.setPattern("(?i.*"); //不正な正規表現
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("invalid pattern was specified. pattern=[(?i.*]"));
        }
    }

    /**
     * 不正な項目名称の正規表現インデックスを指定した場合
     */
    @Test
    public void testInvalidNamePatternIndex() {

        // 正値で不正なインデックス
        try {
            ItemPattern osPattern = new ItemPattern();
            osPattern.setPattern("(?i).*(windows|mac os x)[\\D+]*([\\d\\._]*).*");
            osPattern.setNameIndex(5); // 不正なインデックス


            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("invalid name index was specified. index=[5] pattern=[(?i).*(windows|mac os x)[\\D+]*([\\d\\._]*).*]"));
        }
        try {
            ItemPattern browserPattern = new ItemPattern();
            browserPattern.setPattern("(?i).*(msie\\s|trident.+rv:)([\\d\\.]*).*");
            browserPattern.setNameIndex(5); // 不正なインデックス

            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("invalid name index was specified. index=[5] pattern=[(?i).*(msie\\s|trident.+rv:)([\\d\\.]*).*]"));
        }

        // 負値で不正なインデックス
        try {
            ItemPattern osPattern = new ItemPattern();
            osPattern.setNameIndex(0); // 不正なインデックス
                                                    // インデックス指定とパターン指定の順序に依らずチェックが行われる。
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("nameIndex must not be zero or negative.but was [0]."));
        }

        try {
            ItemPattern browserPattern = new ItemPattern();
            browserPattern.setPattern("(?i).*(msie\\s|trident.+rv:)([\\d\\.]*).*");
            browserPattern.setNameIndex(0); // 不正なインデックス

            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("nameIndex must not be zero or negative.but was [0]."));
        }

    }

    /**
     * 不正なバージョンの正規表現インデックスを指定した場合
     */
    @Test
    public void testInvalidVersionPatternIndex() {

        // 正値で不正なインデックス
        try {
            ItemPattern itemPattern = new ItemPattern();
            // インデックス指定とパターン指定の順序に依らずチェックが行われる。
            itemPattern.setVersionIndex(5); // 不正なインデックス
            itemPattern.setPattern("(?i).*(windows|mac os x)[\\D+]*([\\d\\._]*).*");


            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("invalid version index was specified. index=[5] pattern=[(?i).*(windows|mac os x)[\\D+]*([\\d\\._]*).*]"));
        }

        try {


            ItemPattern itemPattern = new ItemPattern();
            // インデックス指定とパターン指定の順序に依らずチェックが行われる。
            itemPattern.setPattern("(?i).*(msie\\s|trident.+rv:)([\\d\\.]*).*");
            itemPattern.setVersionIndex(5); // 不正なインデックス


            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("invalid version index was specified. index=[5] pattern=[(?i).*(msie\\s|trident.+rv:)([\\d\\.]*).*]"));
        }

        // 0で不正なインデックス
        try {


            ItemPattern itemPattern = new ItemPattern();

            // インデックス指定とパターン指定の順序に依らずチェックが行われる。
            itemPattern.setPattern("(?i).*(windows|mac os x)[\\D+]*([\\d\\._]*).*");
            itemPattern.setVersionIndex(0); // 不正なインデックス

            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("versionIndex must not be zero or negative.but was [0]"));
        }

        try {
            // インデックス指定とパターン指定の順序に依らずチェックが行われる。
            ItemPattern itemPattern = new ItemPattern();
            itemPattern.setVersionIndex(0); // 不正なインデックス
            itemPattern.setPattern("(?i).*(msie\\s|trident.+rv:)([\\d\\.]*).*");

            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("versionIndex must not be zero or negative.but was [0]"));
        }
    }


    /**
     * 解析中にエラーが発生した場合
     */
    @Test
    public void testNameConverterError() {
        final UserAgentValueConvertor errorConvertor = new UserAgentValueConvertor() {
            public String convert(String value) {
                throw new MyException();
            }
        };

        String uaText = "Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko";

        RegexUserAgentParser parser = new RegexUserAgentParser();

        ItemPattern osPattern = new ItemPattern();
        osPattern.setPattern("(?i).*(windows|mac os x)[\\D+]*([\\d\\._]*).*");
        osPattern.setNameIndex(1);
        osPattern.setNameConvertor(errorConvertor);

        UserAgentPatternSetting osSetting = new UserAgentPatternSetting();
        osSetting.addItemPattern(osPattern);
        parser.setOsSetting(osSetting);


        parser.setBrowserSetting(new UserAgentPatternSetting());
        try {
            parser.parse(uaText);
            fail();
        } catch (IllegalStateException e) {
            Throwable cause = e.getCause();
            assertThat(e.toString(),
                    cause, is(instanceOf(MyException.class)));
        }
    }

    private static class MyException extends RuntimeException {
    }

    /** OS設定がセットされていない場合、例外が発生すること。 */
    @Test
    public void testOsSettingNotSet() {
        RegexUserAgentParser target = new RegexUserAgentParser();
        target.setBrowserSetting(new UserAgentPatternSetting());
        try {
            target.parse("");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("osSetting must be set."));
        }
    }

    /** OS設定がセットされていない場合、例外が発生すること。 */
    @Test
    public void testBrowserSettingNotSet() {
        RegexUserAgentParser target = new RegexUserAgentParser();
        target.setOsSetting(new UserAgentPatternSetting());
        try {
            target.parse("");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("browserSetting must be set."));
        }
    }

    /**
     * 標準パーサを指定した場合のUserAgent取得テスト
     */
    @Test
    public void testUserAgentBasicParser() throws IOException {
        RegexUserAgentParser target = getParserFrom("please/change/me/fw/web/useragent/testUserAgentBasicParser.xml");
        UserAgent userAgent = target.parse("Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko");

        assertEquals("Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko", userAgent.getText());
        assertEquals("desktop", userAgent.getOsType());
        assertEquals("windows", userAgent.getOsName());
        assertEquals("-6 --1 ---", userAgent.getOsVersion());
        assertEquals("Gecko", userAgent.getBrowserType());
        assertEquals("ie", userAgent.getBrowserName());
        assertEquals("_11 __0 ___", userAgent.getBrowserVersion());
    }


    /**
     * カスタムパーサを指定した場合のUserAgent取得テスト
     * @throws Exception 例外
     */
    @Test
    public void testUserAgentCustomParser() throws Exception {

        CustomUserAgentParser target = getParserFrom("please/change/me/fw/web/useragent/testUserAgentCustomParser.xml");

        CustomUserAgent userAgent = target.parse("test");


        assertEquals("test", userAgent.getText());
        assertEquals("Unknown", userAgent.getOsType());
        assertEquals("Unknown", userAgent.getOsName());
        assertEquals("Unknown", userAgent.getOsVersion());
        assertEquals("Unknown", userAgent.getBrowserType());
        assertEquals("Unknown", userAgent.getBrowserName());
        assertEquals("Unknown", userAgent.getBrowserVersion());
        assertTrue(userAgent.isTablet());
    }

    /**
     * UserAgentヘッダが無い場合のUserAgent取得テスト
     * @throws Exception 例外
     */
    @Test
    public void testUserAgentNoHeader() throws Exception {

        CustomUserAgentParser target = getParserFrom("please/change/me/fw/web/useragent/testUserAgentCustomParser.xml");
        CustomUserAgent userAgent = target.parse("");

        assertThat(userAgent.getText(), is(""));
        assertEquals("Unknown", userAgent.getOsType());
        assertEquals("Unknown", userAgent.getOsName());
        assertEquals("Unknown", userAgent.getOsVersion());
        assertEquals("Unknown", userAgent.getBrowserType());
        assertEquals("Unknown", userAgent.getBrowserName());
        assertEquals("Unknown", userAgent.getBrowserVersion());
    }

    @Test
    public void testNull() {

        RegexUserAgentParser parser = new RegexUserAgentParser();

        // OS情報
        ItemPattern itemPattern = new ItemPattern();
        itemPattern.setPattern("(?i).*(windows|mac os x)[\\D+]*([\\d\\._]*).*");
        itemPattern.setNameIndex(1);
        itemPattern.setVersionIndex(2);

        // ブラウザ情報
        ItemPattern browserPattern = new ItemPattern();
        browserPattern.setPattern("(?i).*(firefox|chrome)[\\s/]*([\\d\\.]*).*");
        browserPattern.setNameIndex(1);
        browserPattern.setVersionIndex(2);


        UserAgentPatternSetting osSetting = new UserAgentPatternSetting();
        osSetting.addTypePattern(winMacType);
        osSetting.addItemPattern(itemPattern);
        parser.setOsSetting(osSetting);

        UserAgentPatternSetting browserSetting = new UserAgentPatternSetting();
        browserSetting.addTypePattern(ffChromeType);
        browserSetting.addItemPattern(browserPattern);
        parser.setBrowserSetting(browserSetting);

        // nullを設定
        UserAgent userAgent = parser.parse(null);
        assertThat(userAgent, is(notNullValue()));

    }


    public static class CustomUserAgentParser extends RegexUserAgentParser {
        public CustomUserAgentParser() {
        }

        @Override
        public CustomUserAgent parse(String userAgentText) {
            UserAgent userAgent = super.parse(userAgentText);
            return new CustomUserAgent(userAgent)
                        .setTablet(true);
        }
    }
    
    public static class CustomUserAgent extends UserAgent {


        public CustomUserAgent(UserAgent original) {
            super(original);
        }
        
        private boolean isTablet;
        public CustomUserAgent setTablet(boolean isTablet) {
            this.isTablet = isTablet;
            return this;
        }
        public boolean isTablet() {
            return isTablet;
        }
    }

    private static <P extends UserAgentParser> P getParserFrom(String configFilePath) {
        XmlComponentDefinitionLoader loader
                = new XmlComponentDefinitionLoader(configFilePath);
        DiContainer container = new DiContainer(loader);
        return container.getComponentByName("userAgentParser");
    }

}

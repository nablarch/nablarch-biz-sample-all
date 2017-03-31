package please.change.me.fw.web.useragent;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.MockHttpRequest;
import nablarch.fw.web.useragent.UserAgent;
import nablarch.test.RepositoryInitializer;
import nablarch.test.support.SystemRepositoryResource;

import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 * 解説書に記載したUserAgent情報パターンサンプルのテストを行う。
 * 
 * @author TIS
 */
@RunWith(Theories.class)
public class UserAgentSampleTest {
    
    // ----- OS(デバイス)情報とブラウザ情報を抽出し使用する場合 のテストケース -------------------------------------
    
    /** IEのテスト用UserAgentリスト */
    @DataPoints
    public static final UAOsAndBrowserFixture[] IE_TEST_DATA = {
        new UAOsAndBrowserFixture("ie-win7_32_ie11")
            .setText("Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko")
            .setOs("windows").setOsVer("-6 --1 ---").setBrowser("ie").setBrowserVer("_11 __0 ___"),

        new UAOsAndBrowserFixture("ie-win7_32_ie10")
            .setText("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)")
            .setOs("windows").setOsVer("-6 --1 ---").setBrowser("ie").setBrowserVer("_10 __0 ___"),

        new UAOsAndBrowserFixture("ie-win7_64_ie10")
            .setText("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Win64; x64; Trident/6.0) *3")
            .setOs("windows").setOsVer("-6 --1 ---").setBrowser("ie").setBrowserVer("_10 __0 ___"),

        new UAOsAndBrowserFixture("ie-win7_32_ie9")
            .setText("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)")
            .setOs("windows").setOsVer("-6 --1 ---").setBrowser("ie").setBrowserVer("_9 __0 ___"),

        new UAOsAndBrowserFixture("ie-win7_64_ie9")
            .setText("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0)")
            .setOs("windows").setOsVer("-6 --1 ---").setBrowser("ie").setBrowserVer("_9 __0 ___"),

        new UAOsAndBrowserFixture("ie-vista_32_ie9")
            .setText("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.0; Trident/5.0)")
            .setOs("windows").setOsVer("-6 --0 ---").setBrowser("ie").setBrowserVer("_9 __0 ___"),

        new UAOsAndBrowserFixture("ie-vista_64_ie9")
            .setText("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.0; Win64; x64; Trident/5.0)")
            .setOs("windows").setOsVer("-6 --0 ---").setBrowser("ie").setBrowserVer("_9 __0 ___"),

        new UAOsAndBrowserFixture("ie-win7_32_ie8")
            .setText("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0)")
            .setOs("windows").setOsVer("-6 --1 ---").setBrowser("ie").setBrowserVer("_8 __0 ___"),

        new UAOsAndBrowserFixture("ie-win7_64_ie8")
            .setText("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Win64; x64; Trident/4.0)")
            .setOs("windows").setOsVer("-6 --1 ---").setBrowser("ie").setBrowserVer("_8 __0 ___"),

        new UAOsAndBrowserFixture("ie-vista_32_ie8")
            .setText("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0)")
            .setOs("windows").setOsVer("-6 --0 ---").setBrowser("ie").setBrowserVer("_8 __0 ___"),

        new UAOsAndBrowserFixture("ie-vista_64_ie8")
            .setText("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Win64; x64; Trident/4.0)")
            .setOs("windows").setOsVer("-6 --0 ---").setBrowser("ie").setBrowserVer("_8 __0 ___"),
    };

    /** FireFoxのテスト用UserAgentリスト */
    @DataPoints
    public static final UAOsAndBrowserFixture[] FIREFOX_TEST_DATA = {
        new UAOsAndBrowserFixture("FireFox-win7_32")
            .setText("Mozilla/5.0 (Windows NT 6.1; rv:28.0) Gecko/20100101 Firefox/28.0")
            .setOs("windows").setOsVer("-6 --1 ---").setBrowser("firefox").setBrowserVer("_28 __0 ___"),

        new UAOsAndBrowserFixture("FireFox-win7_64")
            .setText("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:28.0) Gecko/20100101 Firefox/28.0")
            .setOs("windows").setOsVer("-6 --1 ---").setBrowser("firefox").setBrowserVer("_28 __0 ___"),

        new UAOsAndBrowserFixture("FireFox-win7_64_2")
            .setText("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:24.0) Gecko/20100101 Firefox/24.0 *21")
            .setOs("windows").setOsVer("-6 --1 ---").setBrowser("firefox").setBrowserVer("_24 __0 ___"),

        new UAOsAndBrowserFixture("FireFox-vista_32")
            .setText("Mozilla/5.0 (Windows NT 6.0; rv:28.0) Gecko/20100101 Firefox/28.0")
            .setOs("windows").setOsVer("-6 --0 ---").setBrowser("firefox").setBrowserVer("_28 __0 ___"),

        new UAOsAndBrowserFixture("FireFox-vista_64")
            .setText("Mozilla/5.0 (Windows NT 6.0; WOW64; rv:28.0) Gecko/20100101 Firefox/28.0")
            .setOs("windows").setOsVer("-6 --0 ---").setBrowser("firefox").setBrowserVer("_28 __0 ___"),

        new UAOsAndBrowserFixture("FireFox-osX_109")
            .setText("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:28.0) Gecko/20100101 Firefox/28.0")
            .setOs("mac_os_x").setOsVer("-10 --9 ---").setBrowser("firefox").setBrowserVer("_28 __0 ___"),

        new UAOsAndBrowserFixture("FireFox-osX_108")
            .setText("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:28.0) Gecko/20100101 Firefox/28.0")
            .setOs("mac_os_x").setOsVer("-10 --8 ---").setBrowser("firefox").setBrowserVer("_28 __0 ___"),

        new UAOsAndBrowserFixture("FireFox-osX_107")
            .setText("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:28.0) Gecko/20100101 Firefox/28.0")
            .setOs("mac_os_x").setOsVer("-10 --7 ---").setBrowser("firefox").setBrowserVer("_28 __0 ___"),
    };

    /** Chromeのテスト用UserAgentリスト */
    @DataPoints
    public static final UAOsAndBrowserFixture[] CHROME_TEST_DATA = {
        new UAOsAndBrowserFixture("Chrome-win7_32")
            .setText("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36")
            .setOs("windows").setOsVer("-6 --1 ---").setBrowser("chrome").setBrowserVer("_34 __0 ___1847"),

        new UAOsAndBrowserFixture("Chrome-win7_64")
            .setText("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36")
            .setOs("windows").setOsVer("-6 --1 ---").setBrowser("chrome").setBrowserVer("_34 __0 ___1847"),

        new UAOsAndBrowserFixture("Chrome-vista_32")
            .setText("Mozilla/5.0 (Windows NT 6.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36")
            .setOs("windows").setOsVer("-6 --0 ---").setBrowser("chrome").setBrowserVer("_34 __0 ___1847"),

        new UAOsAndBrowserFixture("Chrome-vista_64")
            .setText("Mozilla/5.0 (Windows NT 6.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36")
            .setOs("windows").setOsVer("-6 --0 ---").setBrowser("chrome").setBrowserVer("_34 __0 ___1847"),

        new UAOsAndBrowserFixture("Chrome-macOS_10_9")
            .setText("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36")
            .setOs("mac_os_x").setOsVer("-10 --9 ---2").setBrowser("chrome").setBrowserVer("_34 __0 ___1847"),

        new UAOsAndBrowserFixture("Chrome-macOS_10_8")
            .setText("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36")
            .setOs("mac_os_x").setOsVer("-10 --8 ---5").setBrowser("chrome").setBrowserVer("_34 __0 ___1847"),

        new UAOsAndBrowserFixture("Chrome-macOS_10_7")
            .setText("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36")
            .setOs("mac_os_x").setOsVer("-10 --7 ---5").setBrowser("chrome").setBrowserVer("_34 __0 ___1847"),
    };

    /** iPhoneのテスト用UserAgentリスト */
    @DataPoints
    public static final UAOsAndBrowserFixture[] IPHONE_TEST_DATA = {
        new UAOsAndBrowserFixture("iphone-iphone6")
            .setText("Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A403 Safari/8536.25")
            .setOs("iphone").setOsVer("-6 --0 ---").setBrowser("mobile_safari").setBrowserVer("_6 __0 ___"),
    };

    /** iPadのテスト用UserAgentリスト */
    @DataPoints
    public static final UAOsAndBrowserFixture[] IPAD_TEST_DATA = {
        new UAOsAndBrowserFixture("ipad-iphone6")
            .setText("Mozilla/5.0 (iPad; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11A465 Safari/8536.25")
            .setOs("ipad").setOsVer("-6 --0 ---").setBrowser("mobile_safari").setBrowserVer("_7 __0 ___"),
    };

    /** Safariのテスト用UserAgentリスト */
    @DataPoints
    public static final UAOsAndBrowserFixture[] SAFARI_TEST_DATA = {
        new UAOsAndBrowserFixture("safari-macOS_10_9")
            .setText("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/537.75.14")
            .setOs("mac_os_x").setOsVer("-10 --9 ---2").setBrowser("safari").setBrowserVer("_7 __0 ___3"),

        new UAOsAndBrowserFixture("safari-macOS_10_8")
            .setText("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/6.1.3 Safari/537.75.14")
            .setOs("mac_os_x").setOsVer("-10 --8 ---5").setBrowser("safari").setBrowserVer("_6 __1 ___3"),

        new UAOsAndBrowserFixture("safari-macOS_10_7")
            .setText("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/6.1.3 Safari/537.75.14")
            .setOs("mac_os_x").setOsVer("-10 --7 ---5").setBrowser("safari").setBrowserVer("_6 __1 ___3"),
    };

    /** Androidのテスト用UserAgentリスト */
    @DataPoints
    public static final UAOsAndBrowserFixture[] ANDROID_TEST_DATA = {
        new UAOsAndBrowserFixture("android-htc_4_4_2")
            .setText("Mozilla/5.0 (Linux; Android 4.4.2; HTC One 801e Build/KOT49H)"
                   + " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36")
            .setOs("android").setOsVer("-4 --4 ---2").setBrowser("chrome").setBrowserVer("_34 __0 ___1847"),

        new UAOsAndBrowserFixture("android-nexus_4_4_2")
            .setText("Mozilla/5.0 (Linux; Android 4.4.2; Nexus 7 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Safari/537.36")
            .setOs("android").setOsVer("-4 --4 ---2").setBrowser("chrome").setBrowserVer("_34 __0 ___1847"),

        new UAOsAndBrowserFixture("android-so_4_2_2")
            .setText("Mozilla/5.0 (Linux; Android 4.2.2; SO-02E Build/10.3.1.B.0.256)"
                   + " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36")
            .setOs("android").setOsVer("-4 --2 ---2").setBrowser("chrome").setBrowserVer("_34 __0 ___1847"),

        new UAOsAndBrowserFixture("android-nexus7_4_4")
            .setText("Mozilla/5.0 (Linux; Android 4.4; Nexus 7 Build/KRT16S) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.59 Safari/537.36")
            .setOs("android").setOsVer("-4 --4 ---").setBrowser("chrome").setBrowserVer("_31 __0 ___1650"),

        new UAOsAndBrowserFixture("android-nexus5_4_4")
            .setText("Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/KRT16M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.59 Mobile Safari/537.36")
            .setOs("android").setOsVer("-4 --4 ---").setBrowser("chrome").setBrowserVer("_31 __0 ___1650"),

        new UAOsAndBrowserFixture("android-nexus_4_3")
            .setText("Mozilla/5.0 (Linux; Android 4.3; Nexus 7 Build/JSS15Q) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.82 Safari/537.36")
            .setOs("android").setOsVer("-4 --3 ---").setBrowser("chrome").setBrowserVer("_30 __0 ___1599"),

        new UAOsAndBrowserFixture("android-htc_4_2_2")
            .setText("Mozilla/5.0 (Linux; Android 4.2.2; HTC One Build/JDQ39) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.30 Mobile Safari/537.36")
            .setOs("android").setOsVer("-4 --2 ---2").setBrowser("chrome").setBrowserVer("_30 __0 ___1599"),

        new UAOsAndBrowserFixture("android-galaxy_android")
            .setText("Mozilla/5.0 (Linux; U; Android 4.3;ja-jp;SC-03E Build/JSS15J) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")
            .setOs("android").setOsVer("-4 --3 ---").setBrowser("android_browser").setBrowserVer("_4 __0 ___"),

        new UAOsAndBrowserFixture("android-galaxy_chrome")
            .setText("Mozilla/5.0 '(Linux; Android 4.3; SC-03E Build/JSS15J) AppleWebKit/537.36 '(KHTML, like Gecko) Chrome/35.0.1916.122 Mobile Safari/537.36")
            .setOs("android").setOsVer("-4 --3 ---").setBrowser("chrome").setBrowserVer("_35 __0 ___1916"),

        new UAOsAndBrowserFixture("android-galaxy_tab_SC-01C")
            .setText("Mozilla/5.0 (Linux; U; Android 2.2; ja-jp; SC-01C Build/FROYO) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1")
            .setOs("android").setOsVer("-2 --2 ---").setBrowser("android_browser").setBrowserVer("_4 __0 ___"),
    };
    
    
    // ----- ブラウザの種別のみを特定する場合 のテストケース -------------------------------------
    
    /** ブラウザ種別のテスト用UserAgentリスト */
    @DataPoints
    public static final UABrowserTypeFixture[] BROWSER_TYPE = {
        new UABrowserTypeFixture("ie-win7_32_ie10")
            .setText("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)")
            .setBrowserType("MSIE"),
            
        new UABrowserTypeFixture("Chrome-macOS_10_9")
            .setText("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36")
            .setBrowserType("WebKit"),
            
        new UABrowserTypeFixture("ie-win7_32_ie11")
            .setText("Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko")
            .setBrowserType("MSIE"),

        new UABrowserTypeFixture("FireFox-win7_32")
                .setText("Mozilla/5.0 (Windows NT 6.1; rv:28.0) Gecko/20100101 Firefox/28.0")
                .setBrowserType("Gecko"),

        new UABrowserTypeFixture("safari-macOS_10_9")
                    .setText("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/537.75.14")
                    .setBrowserType("WebKit")

    };


    // ----- 任意の解析クラスを実装する場合 のテストケース -------------------------------------
    
    /** 任意解析クラスのテスト用UserAgentリスト */
    @DataPoints
    public static final UACustomParserFixture[] CUSTOM_PARSER = {
        new UACustomParserFixture("ie-win7_32_ie10")
            .setText("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)")
            .setTablet(false).setSmartPhone(false).setFeaturePhone(false),
            
        new UACustomParserFixture("ipad-iphone6")
            .setText("Mozilla/5.0 (iPad; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11A465 Safari/8536.25")
            .setTablet(true).setSmartPhone(false).setFeaturePhone(false),
            
        new UACustomParserFixture("iphone-iphone6")
            .setText("Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A403 Safari/8536.25")
            .setTablet(false).setSmartPhone(true).setFeaturePhone(false),
            
        new UACustomParserFixture("android_tablet")
            .setText("Mozilla/5.0 (Linux; U; Android 3.2; ja-jp; SC-01D Build/MASTER) AppleWebKit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13")
            .setTablet(true).setSmartPhone(false).setFeaturePhone(false),
            
        new UACustomParserFixture("android_smartphone")
            .setText("Mozilla/5.0 (Linux; U; Android 2.3.3; ja-jp; SC-02C Build/GINGERBREAD) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1")
            .setTablet(false).setSmartPhone(true).setFeaturePhone(false),
            
        new UACustomParserFixture("docomo_featurephone")
            .setText("DoCoMo/2.0 N2001(c10)")
            .setTablet(false).setSmartPhone(false).setFeaturePhone(true),
    };

    
    // ----- Fixture -----------------------------------------------------------------------------
    /**
     * UAOsAndBrowserFixture
     */
    public static class UAOsAndBrowserFixture {
        /** ケース */
        private String caseName;
        /** UAテキスト */
        private String text;
        /** OS名 */
        private String os = "Unknown";
        /** OSバージョン */
        private String osVer = "";
        /** ブラウザ名 */
        private String browser = "Unknown";
        /** ブラウザバージョン */
        private String browserVer = "";

        /**
         * コンストラクタ
         */
        UAOsAndBrowserFixture() {
        }
        
        /**
         * コンストラクタ
         * @param caseName ケース名
         */
        UAOsAndBrowserFixture(String caseName) {
            this.caseName = caseName;
        }
        
        /**
         * ケース名取得
         * @return ケース名
         */
        String getCaseName() {
            return this.caseName;
        }
        
        /**
         * OS名設定
         * @param os OS名
         * @return このオブジェクト自体
         */
        UAOsAndBrowserFixture setOs(String os) {
            this.os = os;
            return this;
        }
        
        /**
         * OS名取得
         * @return OS名
         */
        String getOs() {
            return this.os;
        }
        
        /**
         * OSバージョン設定
         * @param osVer OSバージョン
         * @return このオブジェクト自体
         */
        UAOsAndBrowserFixture setOsVer(String osVer) {
            this.osVer = osVer;
            return this;
        }
        
        /**
         * OSバージョン取得
         * @return OSバージョン
         */
        String getOsVer() {
            return this.osVer;
        }
        
        /**
         * ブラウザ名設定
         * @param browser ブラウザ名
         * @return このオブジェクト自体
         */
        UAOsAndBrowserFixture setBrowser(String browser) {
            this.browser = browser;
            return this;
        }
        
        /**
         * ブラウザ名取得
         * @return ブラウザ名
         */
        String getBrowser() {
            return this.browser;
        }
        
        /**
         * ブラウザバージョン設定
         * @param browserVer ブラウザバージョン設定
         * @return このオブジェクト自体
         */
        UAOsAndBrowserFixture setBrowserVer(String browserVer) {
            this.browserVer = browserVer;
            return this;
        }
        
        /**
         * ブラウザバージョン取得
         * @return ブラウザバージョン設定
         */
        String getBrowserVer() {
            return this.browserVer;
        }
        
        /**
         * UAテキスト設定
         * @param text UAテキスト
         * @return このオブジェクト自体
         */
        UAOsAndBrowserFixture setText(String text) {
            this.text = text;
            return this;
        }
        
        /**
         * UAテキスト取得
         * @return UAテキスト
         */
        String getText() {
            return text;
        }
    }

    /**
     * UABrowserTypeFixture
     */
    public static class UABrowserTypeFixture {
        /** ケース */
        private String caseName;
        /** UAテキスト */
        private String text;
        /** ブラウザ種別 */
        private String browserType = "";
        /**
         * コンストラクタ
         */
        UABrowserTypeFixture() {
        }
        
        /**
         * コンストラクタ
         * @param caseName ケース名
         */
        UABrowserTypeFixture(String caseName) {
            this.caseName = caseName;
        }
        
        /**
         * ケース名取得
         * @return ケース名
         */
        String getCaseName() {
            return this.caseName;
        }
        
        /**
         * ブラウザ種別設定
         * @param browserType ブラウザ種別
         * @return このオブジェクト自体
         */
        UABrowserTypeFixture setBrowserType(String browserType) {
            this.browserType = browserType;
            return this;
        }
        
        /**
         * ブラウザ種別取得
         * @return ブラウザ種別
         */
        String getBrowserType() {
            return this.browserType;
        }
        
        /**
         * UAテキスト設定
         * @param text UAテキスト
         * @return このオブジェクト自体
         */
        UABrowserTypeFixture setText(String text) {
            this.text = text;
            return this;
        }
        
        /**
         * UAテキスト取得
         * @return UAテキスト
         */
        String getText() {
            return text;
        }
    }
    
    /**
     * UACustomParserFixture
     */
    public static class UACustomParserFixture {
        /** ケース */
        private String caseName;
        /** UAテキスト */
        private String text;
        /** タブレット */
        private boolean isTablet;
        /** スマートフォン */
        private boolean isSmartPhone;
        /** フィーチャーフォン */
        private boolean isFeaturePhone;
        /**
         * コンストラクタ
         */
        UACustomParserFixture() {
        }
        
        /**
         * コンストラクタ
         * @param caseName ケース名
         */
        UACustomParserFixture(String caseName) {
            this.caseName = caseName;
        }
        
        /**
         * ケース名取得
         * @return ケース名
         */
        String getCaseName() {
            return this.caseName;
        }
        
        /**
         * タブレット設定
         * @param isTablet タブレット
         * @return このオブジェクト自体
         */
        UACustomParserFixture setTablet(boolean isTablet) {
            this.isTablet = isTablet;
            return this;
        }
        
        /**
         * タブレット取得
         * @return タブレット
         */
        boolean isTablet() {
            return this.isTablet;
        }
        
        /**
         * スマートフォン設定
         * @param isSmartPhone スマートフォン
         * @return このオブジェクト自体
         */
        UACustomParserFixture setSmartPhone(boolean isSmartPhone) {
            this.isSmartPhone = isSmartPhone;
            return this;
        }
        
        /**
         * スマートフォン取得
         * @return スマートフォン
         */
        boolean isSmartPhone() {
            return this.isSmartPhone;
        }
        
        /**
         * フィーチャーフォン設定
         * @param isFeaturePhone フィーチャーフォン
         * @return このオブジェクト自体
         */
        UACustomParserFixture setFeaturePhone(boolean isFeaturePhone) {
            this.isFeaturePhone = isFeaturePhone;
            return this;
        }
        
        /**
         * フィーチャーフォン取得
         * @return フィーチャーフォン
         */
        boolean isFeaturePhone() {
            return this.isFeaturePhone;
        }
        
        /**
         * UAテキスト設定
         * @param text UAテキスト
         * @return このオブジェクト自体
         */
        UACustomParserFixture setText(String text) {
            this.text = text;
            return this;
        }
        
        /**
         * UAテキスト取得
         * @return UAテキスト
         */
        String getText() {
            return text;
        }
    }
    
    
    // ----- Theory -----------------------------------------------------------------------------
    
    /**
     * OS(デバイス)情報とブラウザ情報を抽出し使用する場合のテスト実施
     * @param fixture ケースデータ情報
     */
    @Theory
    public void testOsAndBrowser(UAOsAndBrowserFixture fixture) {
        UserAgent userAgent = setAndParseUserAgent(fixture.getText());
        
        assertThat(fixture.getCaseName() + "-os", userAgent.getOsName(), is(fixture.getOs()));
        assertThat(fixture.getCaseName() + "-browser", userAgent.getBrowserName(), is(fixture.getBrowser()));
        assertThat(fixture.getCaseName() + "-osVer", userAgent.getOsVersion(), is(fixture.getOsVer()));
        assertThat(fixture.getCaseName() + "-browserVer", userAgent.getBrowserVersion(), is(fixture.getBrowserVer()));
    }
    
    /**
     * ブラウザの種別のみを特定する場合のテスト実施
     * @param fixture ケースデータ情報
     */
    @Theory
    public void testBrowserType(UABrowserTypeFixture fixture) {
        UserAgent userAgent = setAndParseUserAgent(fixture.getText());
        
        assertThat(fixture.getCaseName(), userAgent.getBrowserType(), is(fixture.getBrowserType()));
    }

    /**
     * 任意の解析クラスを実装する場合のテスト実施
     * @param fixture ケースデータ情報
     */
    @Theory
    public void testDeviceType(UACustomParserFixture fixture) {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "please/change/me/fw/web/useragent/UserAgentSampleCustomParserTest.xml");
        SystemRepository.load(new DiContainer(loader));
        try {
            CustomUserAgent userAgent = (CustomUserAgent) setAndParseUserAgent(fixture.getText());
            
            assertThat(fixture.getCaseName(), userAgent.isTablet(), is(fixture.isTablet()));
            assertThat(fixture.getCaseName(), userAgent.isSmartPhone(), is(fixture.isSmartPhone()));
            assertThat(fixture.getCaseName(), userAgent.isFeaturePhone(), is(fixture.isFeaturePhone()));
            
        } finally {
            loader = new XmlComponentDefinitionLoader(
                    "please/change/me/fw/web/useragent/UserAgentSampleTest.xml");
            SystemRepository.load(new DiContainer(loader));
        }
        
    }
    
    /**
     * HttpRequestにUserAgentを設定し、解析を行う。
     * 
     * @param uaText UserAgent文字列
     * @return 解析結果
     */
    private UserAgent setAndParseUserAgent(String uaText) {
        HttpRequest req = new MockHttpRequest();
        req.getHeaderMap().put("User-Agent", uaText);
        return req.getUserAgent();
    }

    @ClassRule
    public static final SystemRepositoryResource RESOURCE = new SystemRepositoryResource(
            "please/change/me/fw/web/useragent/UserAgentSampleTest.xml");

    /**
     * テストクラスの終了処理。
     * 
     * このテスト用に読み込んだコンポーネント定義をクリアする
     * 
     * @throws Exception 例外
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    	RepositoryInitializer.initializeDefaultRepository();
    }

}

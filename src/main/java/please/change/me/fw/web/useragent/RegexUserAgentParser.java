package please.change.me.fw.web.useragent;

import nablarch.core.util.StringUtil;
import nablarch.fw.web.useragent.UserAgent;
import nablarch.fw.web.useragent.UserAgentParser;

import java.util.List;
import java.util.regex.Matcher;

/**
 * UserAgent解析を行う{@link UserAgentParser}実装クラス。
 * 本クラスは、正規表現を用いて解析処理を行う。
 * 正規表現等の設定値は{@link UserAgentPatternSetting}が保持する。
 *
 * @author TIS
 */
public class RegexUserAgentParser implements UserAgentParser {

    /** ブラウザ情報解析用設定値 */
    private UserAgentPatternSetting browserSetting;

    /** OS情報解析用設定値 */
    private UserAgentPatternSetting osSetting;

    /**
     * ブラウザ情報解析用設定値を設定する。
     *
     * @param browserSetting ブラウザ情報解析用設定値
     */
    public void setBrowserSetting(UserAgentPatternSetting browserSetting) {
        this.browserSetting = browserSetting;
    }

    /**
     * OS情報解析用設定値を設定する。
     *
     * @param osSetting OS情報解析用設定値
     */
    public void setOsSetting(UserAgentPatternSetting osSetting) {
        this.osSetting = osSetting;
    }

    /**
     * UserAgent文字列を解析する。
     *
     * @param userAgentText UserAgent文字列
     * @return UserAgent文字列を解析した結果のMap
     */
    public UserAgent parse(String userAgentText) {
        checkSettings();
        String uaText = StringUtil.nullToEmpty(userAgentText);
        UserAgentItem browserItem = new Agent(browserSetting, uaText).parsePattern();
        UserAgentItem osItem = new Agent(osSetting, uaText).parsePattern();

        UserAgent ua = new UserAgent(uaText);
        ua.setBrowserType(browserItem.type);
        ua.setBrowserName(browserItem.name);
        ua.setBrowserVersion(browserItem.version);
        ua.setOsType(osItem.type);
        ua.setOsName(osItem.name);
        ua.setOsVersion(osItem.version);
        return ua;
    }

    /**
     * 設定の確認を行う。
     * 以下の確認項目を満たしていない場合、例外が発生する。
     * <ul>
     * <li>{@link #osSetting}がnullでないか</li>
     * <li>{@link #browserSetting}がnullでないか</li>
     * </ul>
     */
    private void checkSettings() {
        if (osSetting == null) {
            throw new IllegalStateException("osSetting must be set.");
        }
        if (browserSetting == null) {
            throw new IllegalStateException("browserSetting must be set.");
        }
    }

    /**
     * 実際にパースを行うクラス。
     */
    private static final class Agent {

        /** 設定クラス */
        private final UserAgentPatternSetting setting;

        /** User-Agent文字列 */
        private final String uaText;

        /**
         * コンストラクタ。
         *
         * @param setting 設定クラス
         * @param uaText  User-Agent文字列
         */
        private Agent(UserAgentPatternSetting setting, String uaText) {
            this.setting = setting;
            this.uaText = uaText;
        }

        /**
         * UserAgentパターン定義グループの設定に従い、UserAgent文字列を解析する。
         *
         * @return UserAgentItem 解析結果を格納したUserAgent情報
         */
        UserAgentItem parsePattern() {
            // 項目タイプ
            String type = parseType();
            // UserAgent情報
            return parseItem(type);
        }

        /**
         * 項目タイプを解析する。
         *
         * @return 項目タイプ
         */
        private String parseType() {

            List<TypePattern> typeList = setting.getTypePatternList();
            for (TypePattern typePtn : typeList) {
                String type = typePtn.getName(uaText);
                if (type != null) {
                    return type;
                }
            }
            return setting.getDefaultType();
        }

        /**
         * 項目名称、バージョンを解析する。
         *
         * @param type 項目タイプ
         * @return UserAgent情報
         */
        private UserAgentItem parseItem(String type) {
            List<ItemPattern> itemList = setting.getItemPatternList();
            for (ItemPattern itemPtn : itemList) {
                try {
                    Matcher matcher = itemPtn.getMatcherOf(uaText);
                    if (matcher.matches()) {
                        // パターンに合致したらその時点の項目値を設定し、終了
                        return createItem(itemPtn, type);
                    }
                } catch (RuntimeException e) {
                    throw new IllegalStateException(
                            "exception occured in getting item pattern."
                                    + "item pattern=[" + itemPtn + "]"
                            , e);
                }
            }
            // いずれのパターンにも合致しない場合、全項目デフォルト値
            return createDefaultItem(type);
        }

        /**
         * 全項目デフォルト値を設定したUserAgent情報を作成する。
         *
         * @param type 解析済みの項目タイプ、nullの場合デフォルト値が適用される
         * @return UserAgent情報
         */
        private UserAgentItem createDefaultItem(String type) {
            UserAgentItem item = new UserAgentItem();
            item.name = setting.getDefaultName();
            item.version = setting.getDefaultVersion();
            item.type = type;
            return item;
        }

        /**
         * 指定されたパターン定義から取得した値を設定したUserAgent情報を作成する。
         *
         * @param itemPattern UserAgentパターン定義
         * @param type        項目タイプ
         * @return UserAgent情報
         */
        private UserAgentItem createItem(ItemPattern itemPattern, String type) {

            UserAgentItem item = new UserAgentItem();
            // 項目名称
            item.name = itemPattern.getName(uaText);
            if (item.name == null) {
                item.name = setting.getDefaultName();
            }

            // バージョン
            item.version = itemPattern.getVersion(uaText);
            if (item.version == null) {
                item.version = setting.getDefaultVersion();
            }

            item.type = type;
            return item;

        }

    }

    /**
     * UserAgent情報クラス。<br>
     *
     * @author TIS
     */
    private static final class UserAgentItem {

        /** プライベートコンストラクタ。*/
        private UserAgentItem() {
        }

        /** 項目タイプ */
        private String type;
        /** 項目名称 */
        private String name;
        /** バージョン */
        private String version;

    }


}

package please.change.me.fw.web.useragent;

import nablarch.fw.web.useragent.UserAgent;

/**
 * テスト用のカスタムUserAgent
 *
 * @author TIS
 */
public class CustomUserAgent extends UserAgent {
    /** タブレットであるか */

    private boolean isTablet;

    /** スマートフォンであるか */
    private boolean isSmartPhone;

    /** フィーチャーフォンであるか */
    private boolean isFeaturePhone;

    /**
     * コンストラクタ
     *
     * @param original デフォルトパーサの解析結果
     */
    public CustomUserAgent(UserAgent original) {
        super(original);
    }

    /**
     * isTabletを取得する。
     *
     * @return isTablet
     */
    public boolean isTablet() {
        return isTablet;
    }

    /**
     * isTabletをセットする。
     *
     * @param isTablet セットする isTablet
     * @return this
     */
    public CustomUserAgent setTablet(boolean isTablet) {
        this.isTablet = isTablet;
        return this;
    }

    /**
     * isSmartPhoneを取得する。
     *
     * @return isSmartPhone
     */
    public boolean isSmartPhone() {
        return isSmartPhone;
    }

    /**
     * isSmartPhoneをセットする。
     *
     * @param isSmartPhone セットする isSmartPhone
     */
    public void setSmartPhone(boolean isSmartPhone) {
        this.isSmartPhone = isSmartPhone;
    }

    /**
     * isFeaturePhoneを取得する。
     *
     * @return isFeaturePhone
     */
    public boolean isFeaturePhone() {
        return isFeaturePhone;
    }

    /**
     * isFeaturePhoneをセットする。
     *
     * @param isFeaturePhone セットする isFeaturePhone
     */
    public void setFeaturePhone(boolean isFeaturePhone) {
        this.isFeaturePhone = isFeaturePhone;
    }

}

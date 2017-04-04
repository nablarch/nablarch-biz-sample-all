package please.change.me.fw.web.useragent;

import nablarch.fw.web.useragent.UserAgent;

import java.util.Arrays;
import java.util.List;

/**
 * テスト用のカスタムパーサー
 *
 * @author TIS
 */
public class CustomUserAgentParser extends RegexUserAgentParser {

    /** {@inheritDoc} */
    @Override
    public CustomUserAgent parse(String userAgentText) {
        UserAgent userAgent = super.parse(userAgentText);
        CustomUserAgent custom = new CustomUserAgent(userAgent);
        custom.setTablet(isTablet(userAgent));
        custom.setSmartPhone(isSmartPhone(userAgent));
        custom.setFeaturePhone(isFeaturePhone(userAgent));
        return custom;
    }

    /**
     * タブレットであるかを判定する。
     *
     * @param userAgent 解析済みの{@link UserAgent}
     * @return タブレットの場合、真
     */
    private boolean isTablet(UserAgent userAgent) {
        // OS名およびOSタイプにより判定する
        String osName = userAgent.getOsName();
        if (osName.equals("ipad")) {
            return true;

        }
        return osName.equals("android") && userAgent.getOsType().equals("tablet");
    }

    /**
     * スマートフォンであるかを判定する。
     *
     * @param userAgent 解析済みの{@link UserAgent}
     * @return スマートフォンの場合、真
     */
    private boolean isSmartPhone(UserAgent userAgent) {
        // OS名およびOSタイプにより判定する
        String osName = userAgent.getOsName();
        if (osName.equals("iphone")) {
            return true;
        }
        return osName.equals("android") && userAgent.getOsType().equals("mobilePhone");
    }

    /** キャリア名 */
    private static final List<String> CARRIERS =  Arrays.asList(
            "DoCoMo", "kddi", "vodafone"
    );

    /**
     * フィーチャーフォンであるかを判定する。
     *
     * @param userAgent 解析済みの{@link UserAgent}
     * @return フィーチャーフォンの場合、真
     */
    private boolean isFeaturePhone(UserAgent userAgent) {

        // タブレットでもスマートフォンでもなく、キャリア名が含まれる場合
        if (isTablet(userAgent)) {
            return false;
        }
        if (isSmartPhone(userAgent)) {
            return false;
        }
        // UserAgent文字列にキャリア名が含まれるか否かで判定する
        String uaText = userAgent.getText();
        for (String carrier : CARRIERS) {
            if (uaText.contains(carrier)) {
                return true;
            }
        }
        return false;
    }
}

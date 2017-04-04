package please.change.me.fw.web.useragent;

import org.junit.Test;

/**
 * {@link UserAgentPatternSetting}のテスト。
 *
 * @author T.Kawasaki
 */
public class UserAgentPatternSettingTest {

    /** テスト対象 */
    private UserAgentPatternSetting target = new UserAgentPatternSetting();

    /** パターンリストにnullを設定した場合、例外が発生すること。 */
    @Test(expected = IllegalArgumentException.class)
    public void testSetItemPatternListNull() {
        target.setItemPatternList(null);
    }

    /** パターンリストにnullを設定した場合、例外が発生すること。 */
    @Test(expected = IllegalArgumentException.class)
    public void testSetTypePatternListNull() {
        target.setTypePatternList(null);
    }

}
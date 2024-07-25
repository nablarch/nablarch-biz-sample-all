package please.change.me.common.mail.html;

import java.lang.reflect.Constructor;

import org.junit.Test;

/**
 * {@link ContentType}のテスト。
 * 
 * @author tani takanori
 */
public class ContentTypeTest {

    /**
     * プライベートコンストラクタのカバレッジ対応。
     * 
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void testConstructor() throws Exception {
        Constructor<ContentType> constructor = ContentType.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
    
}

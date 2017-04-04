package please.change.me.fw.web.useragent;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * {@link ItemPattern}のテスト。
 *
 * @author T.Kawasaki
 */
public class ItemPatternTest {

    /** コンバータにnullを設定した場合、例外が発生すること。 */
    @Test(expected = IllegalArgumentException.class)
    public void testSetNameConverterNull() {
        new ItemPattern().setNameConvertor(null);
    }

    /** コンバータにnullを設定した場合、例外が発生すること。 */
    @Test(expected = IllegalArgumentException.class)
    public void testSetVersionConverterNull() {
        new ItemPattern().setVersionConvertor(null);
    }

    /** 情報取得時、マッチしない場合はnullが返却されること。*/
    @Test
    public void testGetNotMatch() {
        ItemPattern target = new ItemPattern();
        target.setName("ie");
        target.setPattern("(?i).*(msie\\s|trident.+rv:)([\\d\\.]*).*");
        target.setVersionIndex(2);

        String uaText = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36";

        assertThat(target.getVersion(uaText), is(nullValue()));
        assertThat(target.getName(uaText), is(nullValue()));
    }
    
    /** パターン未設定の場合はチェック */
    @Test
    public void testSetNameIndex() {
    	
    	try {
            new ItemPattern().setNameIndex(1);
    	} catch (Exception e) {
    		fail("エラーは発生しないので、ここは通過しない");
		}

        
    }

}
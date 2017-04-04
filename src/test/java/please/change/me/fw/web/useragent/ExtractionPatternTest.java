package please.change.me.fw.web.useragent;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link ExtractionPattern}のテスト。
 *
 * @author T.Kawasaki
 */
public class ExtractionPatternTest {

    /** 抽出パターン設定前にパターンを取得した場合、例外が発生すること。*/
    @Test(expected = IllegalStateException.class)
    public void test() {
        ExtractionPattern target = new ExtractionPattern() {};
        assertThat(target.isAlreadyPatternSet(), is(false));
        target.getPattern();
    }
}
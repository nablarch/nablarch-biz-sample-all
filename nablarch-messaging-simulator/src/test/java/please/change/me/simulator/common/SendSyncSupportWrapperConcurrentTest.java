package please.change.me.simulator.common;

import nablarch.test.core.reader.DataType;
import org.junit.Test;
import please.change.me.simulator.common.SendSyncSupportWrapperConcurrent.Reader;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

/**
 * {@link SendSyncSupportWrapperConcurrent}のテスト。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class SendSyncSupportWrapperConcurrentTest {

    /** プレースホルダとして使用する引数 */
    private CacheKey dummyKey = new CacheKey(DataType.RESPONSE_BODY_MESSAGES, "dummy");

    /** データ読み取り時、全メッセージを読み取った場合、例外が発生しないこと。*/
    @Test
    public void testReadAll() {
        Reader target = new Reader(dummyKey) {
            @Override
            Object readRecord(DataType dataRecord, String requestId) {
                throw new RuntimeException("receive message did not exists");
            }
        };

        List result = target.readAll();
        assertThat(result.isEmpty(), is(true));
    }

    /** データ読み取り時、予期しない例外が発生した場合、その例外がそのまま送出されること。*/
    @Test
    public void testReadAllFail() {
        Reader target = new Reader(dummyKey) {
            @Override
            Object readRecord(DataType dataRecord, String requestId) {
                throw new NullPointerException("for test");
            }
        };
        try {
            target.readAll();
            fail();
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("for test"));
        }
    }
}
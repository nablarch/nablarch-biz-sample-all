package please.change.me.simulator.outgoing;

import nablarch.core.dataformat.DataRecord;
import nablarch.fw.ExecutionContext;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link CyclicDataReader}のテスト。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class CyclicDataReaderTest {

    /** 読み込み元のデータ */
    private List<DataRecord> src = Arrays.asList(
            new DataRecord().setRecordNumber(1),
            new DataRecord().setRecordNumber(2),
            new DataRecord().setRecordNumber(3));

    @Test
    public void test() {
        CyclicDataReader target = new CyclicDataReader(src, 7);    // 最大7回繰り返す。
        ExecutionContext ctx = new ExecutionContext();
        assertThat(target.hasNext(ctx), is(true));
        assertThat(target.read(ctx).getRecordNumber(), is(1)); // 1
        assertThat(target.read(ctx).getRecordNumber(), is(2)); // 2
        assertThat(target.read(ctx).getRecordNumber(), is(3)); // 3
        assertThat(target.read(ctx).getRecordNumber(), is(1)); // 4
        assertThat(target.read(ctx).getRecordNumber(), is(2)); // 5
        assertThat(target.read(ctx).getRecordNumber(), is(3)); // 6
        assertThat(target.read(ctx).getRecordNumber(), is(1)); // 7

        // 8回目以降は読み出せない。
        assertThat(target.hasNext(ctx), is(false));
        assertThat(target.read(ctx), is(nullValue()));

        target.close(ctx);
    }
}
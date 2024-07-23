package please.change.me.simulator.common.concurrent;

import org.junit.After;
import org.junit.Test;
import please.change.me.simulator.common.FrequencyCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link CyclicCounter}のテストクラス。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class CyclicCounterTest {

    /** シングルスレッドで正しく動作すること。 */
    @Test
    public void testSingleThreadCyclic() {
        CyclicCounter target = new CyclicCounter(3);  // 上限３
        assertThat(target.getAndIncrement(), is(0));
        assertThat(target.getAndIncrement(), is(1));
        assertThat(target.getAndIncrement(), is(2));
        assertThat(target.getAndIncrement(), is(3));
        // 上限に達したので０に戻る。
        assertThat(target.getAndIncrement(), is(0));
        assertThat(target.getAndIncrement(), is(1));
        assertThat(target.getAndIncrement(), is(2));
        assertThat(target.getAndIncrement(), is(3));
        // 上限に達したので０に戻る。
        assertThat(target.getAndIncrement(), is(0));
    }

    private ExecutorService service;

    @After
    public void shutdown() {
        if (service != null) {
            service.shutdownNow();
        }
    }

    @Test
    public void testMultiThread() throws ExecutionException, InterruptedException {
        service = Executors.newFixedThreadPool(100);
        int cntMax = 9;
        // 0～99採番されるカウンタ
        final CyclicCounter target = new CyclicCounter(cntMax);

        // 0～99が10000回採番されるように呼び出し回数を設定
        int repeat = 10000;
        final int methodCallCount = (cntMax + 1) * repeat;
        List<Future<Integer>> results = new ArrayList<Future<Integer>>(methodCallCount);
        for (int i = 0; i < methodCallCount; i++) {
            results.add(service.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return target.getAndIncrement();
                }
            }));
        }
        service.shutdown();
        // 呼び出し回数分だけ採番されていること
        assertThat(results.size(), is(methodCallCount));

        // 採番された数値の出現頻度を集計する。
        FrequencyCounter<Integer> counter = new FrequencyCounter<Integer>();
        for (Future<Integer> e : results) {
            Integer no = e.get();
            counter.count(no);
        }

        // 0～99までが採番されていること。
        int expected = 0;
        for (Integer no : counter.keySet()) {
            assertThat(no, is(expected++));
        }
        assertThat(expected, is(cntMax + 1));

        // 採番された数値の出現頻度が一様であること。
        for (Integer cnt : counter.values()) {
            assertThat(cnt, is(repeat));
        }

    }

    @Test(timeout = 60 * 1000)
    public void testLongRun() {
        int max = 5;
        CyclicCounter target = new CyclicCounter(max);
        for (int i = 0; i < 1000000; i++) {
            for (int j = 0; j <= max; j++) {
                assertThat(target.getAndIncrement(), is(j));
            }
        }
    }

}
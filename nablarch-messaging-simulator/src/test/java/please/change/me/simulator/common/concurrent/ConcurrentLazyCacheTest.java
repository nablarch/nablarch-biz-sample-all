package please.change.me.simulator.common.concurrent;

import org.junit.After;
import org.junit.Test;
import please.change.me.simulator.common.concurrent.ConcurrentLazyCache.CachingValueFactory;
import please.change.me.simulator.common.concurrent.ConcurrentLazyCache.ValueFactory;
import please.change.me.simulator.common.concurrent.ConcurrentLazyCache.ValueFactoryBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link ConcurrentLazyCache}のテストクラス。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class ConcurrentLazyCacheTest {

    /** テスト対象インスタンス */
    private final ConcurrentLazyCache<String, MyValue> target = new ConcurrentLazyCache<String,
            MyValue>(new MyBuilder());

    /** 呼び出し回数を数えるためのカウンタ */
    private final AtomicInteger callCount = new AtomicInteger(0); //マルチスレッドでカウントできるようにAtomicInteger

    /** テスト用の{@link ValueFactoryBuilder}実装。 */
    private class MyBuilder implements ValueFactoryBuilder<String, MyValue> {

        /** {@inheritDoc} */
        @Override
        public ConcurrentLazyCache.ValueFactory<MyValue> newInstance(String key) {
            return new CachingValueFactory<String, MyValue>(key) {
                @Override
                public MyValue getValueOf(String key) {
                    // 呼び出し回数を記録
                    callCount.incrementAndGet();

                    // 毎回別のインスタンスを作る。
                    // 異なるスレッドから複数回呼び出されるとそれぞれ
                    // 別のインスタンスが返却されることになる。
                    return new MyValue(key);
                }

            };
        }
    }

    /** キャッシュに格納されるされる値クラス。 */
    private static class MyValue {

        /** 文字列 */
        private final String s;

        /**
         * コンストラクタ。
         *
         * @param s 文字列
         */
        MyValue(String s) {
            this.s = s;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return s;
        }
    }


    /** シングルスレッド下で動作することを確認する。 */
    @Test
    public void testSingleThread() {
        for (int i = 0; i < 3; i++) {
            final int cnt = 10000;
            List<MyValue> ret = new ArrayList<MyValue>(cnt);
            for (int j = 0; j < cnt; j++) {
                String key = String.valueOf(j);
                MyValue o = target.get(key);
                ret.add(o);
            }
            assertThat(ret.size(), is(10000));
            // 重複がないこと
            assertThat(new HashSet<MyValue>(ret).size(), is(10000));
            // 呼び出し回数分カウントアップされていること。
            assertThat(callCount.get(), is(10000));
        }
    }

    private ExecutorService service;

    /**
     * マルチスレッド下で動作することを確認する。
     *
     * @throws Exception 例外
     */
    @Test
    public void testMultiThread() throws Exception {

        service = Executors.newFixedThreadPool(100);

        final int cnt = 100000;
        List<Callable<MyValue>> callables = new ArrayList<Callable<MyValue>>(cnt);

        for (int i = 0; i < cnt; i++) {
            final String key = new String("aaa");   // 敢えて別インスタンスを生成する。
            callables.add(new Callable<MyValue>() {
                @Override
                public MyValue call() throws Exception {
                    // 全スレッドで同じキー"aaa"をgetする。
                    return target.get(key);
                }
            });
        }

        List<Future<MyValue>> futures = service.invokeAll(callables);

        // 呼び出し回数と結果数が一致していること。
        assertThat(futures.size(), is(cnt));


        // 結果の種類をまとめる。
        // MyObjはequalsをオーバーライドしていないので、
        // インスタンスが同じでないと等価とみなされない。
        Set<MyValue> results = new HashSet<MyValue>();
        for (Future<MyValue> future : futures) {
            results.add(future.get());
        }

        // 一つのキーに対して初期化が1回しか行われていない。
        assertThat(results.size(), is(1));

        //getValueOfが１回しか呼び出されていないこと（それ以外はキャッシュにあたっている）。
        assertThat(callCount.get(), is(1));
    }

    @After
    public void shutdown() {
        if (service != null) {
            service.shutdownNow();
        }
    }


    @Test(expected = IllegalStateException.class)
    public void testInvalidFactory() {

        ConcurrentLazyCache<String, String> target
                = new ConcurrentLazyCache<String, String>(new ValueFactoryBuilder<String, String>() {
            @Override
            public ValueFactory<String> newInstance(String key) {
                return new CachingValueFactory<String, String>("key") {
                    @Override
                    protected String getValueOf(String key) {
                        return null;
                    }
                };
            }
        });
        target.get("will cause exception..");
    }

}
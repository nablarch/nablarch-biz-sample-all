package please.change.me.simulator.incoming;

import please.change.me.simulator.common.concurrent.ExceptionUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * HTTPリクエストを発行するクラス。
 * （テスト用）
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
class HttpRequester {
    /** 同時実行スレッド数デフォルト値 */
    private static final int DEFAULT_THREAD_NUM = 10;

    /** 同時実行スレッド数 */
    private final int threadNum;

    /** リクエスト発行数 */
    private final int execCount;

    /** スレッドプール */
    private ExecutorService pool;

    /**
     * コンストラクタ。
     *
     * @param execCount リクエスト発行数
     */
    HttpRequester(int execCount) {
        this(execCount, DEFAULT_THREAD_NUM);
    }

    HttpRequester(int execCount, int threadNum) {
        this.threadNum = threadNum;
        this.execCount = execCount;
        initPool();
    }

    /** スレッドプールの初期化を行う。 */
    private void initPool() {
        pool = Executors.newFixedThreadPool(threadNum);
    }

    /**
     * 終了処理を行う。
     * スレッドプールのシャットダウンを実行する。
     */
    void terminate() {
        pool.shutdown();
        try {
            boolean success = pool.awaitTermination(5, TimeUnit.SECONDS);
            if (!success) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * レスポンスを保持しないでリクエストを発行する。
     *
     * @param url URL
     * @return 全リクエスト発行に要した時間（ミリ秒）
     */
    long requestWithoutResult(URL url) {
        long start = System.currentTimeMillis();
        try {
            doRequestWithoutResult(url);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            pool.shutdownNow();
            throw ExceptionUtil.convertToRuntime(cause);
        } catch (InterruptedException e) {
            throw ExceptionUtil.convertToRuntime(e);
        } finally {
            terminate();
            initPool();
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    /**
     * レスポンスを保持しつつリクエストを発行する。
     *
     * @param url URL
     * @return レスポンス文字列
     */
    List<String> requestWithResult(URL url) {
        List<Future<String>> futures = null;
        try {
            List<Callable<String>> callables = createCallables(url);
            futures = pool.invokeAll(callables);
            return gatherResult(futures);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            for (Future<String> future : futures) {
                future.cancel(true);
            }
            throw ExceptionUtil.convertToRuntime(cause);
        } catch (InterruptedException e) {
            throw ExceptionUtil.convertToRuntime(e);
        }
    }

    /**
     * レスポンスを保持しないでリクエストを発行する。
     *
     * @param url URL
     * @throws ExecutionException   実行スレッドで例外が発生した場合
     * @throws InterruptedException 割り込みが発生した場合
     */
    private void doRequestWithoutResult(URL url)
            throws ExecutionException, InterruptedException {
        for (int i = 0; i < execCount; i++) {
            pool.submit(new Client(url));
        }
    }

    /**
     * スレッド実行結果を収集する。
     *
     * @param futures {@link Future}
     * @return 実行結果
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private <V> List<V> gatherResult(List<Future<V>> futures) throws ExecutionException, InterruptedException {
        List<V> result = new ArrayList<V>(execCount);
        for (Future<V> future : futures) {
            V s = future.get();
            result.add(s);
        }
        return result;
    }

    /**
     * {@link Callable}インスタンスをリクエスト発行数分だけ作成する。
     *
     * @param url アクセス先URL
     * @return {@link Callable}インスタンス。
     */
    private List<Callable<String>> createCallables(URL url) {
        List<Callable<String>> callables = new ArrayList<Callable<String>>(execCount);
        for (int i = 0; i < execCount; i++) {
            callables.add(new Client(url));
        }
        return callables;
    }


    /**
     * 別スレッドで実行される{@link Callable}実装クラス。
     */
    private static class Client implements Callable<String> {

        /** アクセス先URL */
        private final URL url;

        /** HTTPメソッド */
        private final String method;

        /**
         * コンストラクタ。
         *
         * @param url アクセス先URL
         */
        private Client(URL url) {
            this(url, "GET");
        }

        /**
         * コンストラクタ。
         *
         * @param url    アクセス先URL
         * @param method HTTPメソッド
         */
        private Client(URL url, String method) {
            this.url = url;
            this.method = method;
        }

        /** {@inheritDoc} */
        @Override
        public String call() throws Exception {

            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                InputStream in = conn.getInputStream();
                return readAll(in, Charset.forName("UTF-8"));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        /**
         * 入力ストリームから全データを読み取る。
         *
         * @param in 読み取り元入力ストリーム
         * @return 読み取った文字列
         * @throws IOException
         */
        private String readAll(InputStream in, Charset charset) throws IOException {
            StringBuilder ret = new StringBuilder();
            Reader reader = new BufferedReader(new InputStreamReader(in, charset));
            for (int c; (c = reader.read()) != -1; ) {
                ret.append((char) c);
            }
            return ret.toString();
        }
    }
}

package please.change.me.simulator.common;

import nablarch.core.dataformat.DataRecord;
import nablarch.test.core.messaging.SendSyncSupport;
import nablarch.test.core.reader.DataType;
import please.change.me.simulator.common.concurrent.ConcurrentLazyCache;
import please.change.me.simulator.common.concurrent.ConcurrentLazyCache.CachingValueFactory;
import please.change.me.simulator.common.concurrent.ConcurrentLazyCache.ValueFactory;
import please.change.me.simulator.common.concurrent.ConcurrentLazyCache.ValueFactoryBuilder;
import please.change.me.simulator.common.concurrent.CyclicIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link SendSyncSupportWrapper}の実装クラス。
 * <p/>
 * データタイプ、リクエストID毎にデータをキャッシュする。
 * 本インスタンスはSingletonとして使用する。
 * インスタンスごとにキャッシュを持つので、複数インスタンス生成すると、
 * キャッシュも複数できてしまうからである。
  * <p/>
 * 本クラスはスレッドセーフである。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public final class SendSyncSupportWrapperConcurrent implements SendSyncSupportWrapper {

    /** シングルトンインスタンス */
    private static final SendSyncSupportWrapperConcurrent soloInstance  // SUPPRESS CHECKSTYLE 定数ではないため
            = new SendSyncSupportWrapperConcurrent();

    /**
     * インスタンスを取得する。
     * @return 本クラスのインスタンス
     */
    public static SendSyncSupportWrapper getInstance() {
        return soloInstance;
    }

    /** プライベートコンストラクタ。*/
    private SendSyncSupportWrapperConcurrent() {
    }

    /** Excelから読み取ったメッセージのキャッシュ。 */
    private final ConcurrentLazyCache<CacheKey, CyclicIterator<DataRecord>> records
            = new ConcurrentLazyCache<CacheKey, CyclicIterator<DataRecord>>(new DataRecordFactoryBuilder());

    /** Excelから読み取ったメッセージのキャッシュ。 */
    private final ConcurrentLazyCache<CacheKey, CyclicIterator<byte[]>> bytesMap
            = new ConcurrentLazyCache<CacheKey, CyclicIterator<byte[]>>(new BytesFactoryBuilder());


    /** {@inheritDoc} */
    @Override
    public DataRecord getResponseMessageByRequestId(DataType dataType, String requestId) {
        CacheKey key = new CacheKey(dataType, requestId);
        CyclicIterator<DataRecord> itr = records.get(key);
        return itr.next();
    }

    /** {@inheritDoc} */
    @Override
    public byte[] getResponseMessageBinaryByRequestId(DataType dataType, String requestId) {
        CacheKey key = new CacheKey(dataType, requestId);
        CyclicIterator<byte[]> itr = bytesMap.get(key);
        return itr.next();
    }

    /**
     * ファクトリの基底クラス
     *
     * @param <DATA> 取得するデータの型
     */
    private abstract static class FactoryBase<DATA> extends CachingValueFactory<CacheKey, DATA> {

        /**
         * コンストラクタ。
         *
         * @param key キー
         */
        private FactoryBase(CacheKey key) {
            super(key);
        }

    }

    /**
     * データレコードを返却するファクトリを生成するクラス。
     */
    private static class DataRecordFactoryBuilder implements ValueFactoryBuilder<CacheKey, CyclicIterator<DataRecord>> {

        /** {@inheritDoc} */
        @Override
        public ValueFactory<CyclicIterator<DataRecord>> newInstance(CacheKey key) {
            return new DataRecordFactory(key);
        }

        /**
         * データレコードを返却するファクトリクラス。
         */
        private static class DataRecordFactory extends FactoryBase<CyclicIterator<DataRecord>> {

            /**
             * コンストラクタ。
             *
             * @param key キー
             */
            public DataRecordFactory(CacheKey key) {
                super(key);
            }

            /** {@inheritDoc} */
            @Override
            protected CyclicIterator<DataRecord> getValueOf(CacheKey key) {
                Reader<DataRecord> reader = new Reader<DataRecord>(key) {
                    @Override
                    DataRecord readRecord(DataType dataRecord, String requestId) {
                        return support.getResponseMessageByRequestId(dataRecord, requestId);
                    }
                };
                List<DataRecord> dataRecords = reader.readAll();
                return new CyclicIterator<DataRecord>(dataRecords);
            }
        }
    }

    /**
     * バイトレコードを返却するファクトリを生成するクラス。
     */
    private static class BytesFactoryBuilder implements ValueFactoryBuilder<CacheKey, CyclicIterator<byte[]>> {

        /** {@inheritDoc} */
        @Override
        public ValueFactory<CyclicIterator<byte[]>> newInstance(CacheKey key) {
            return new BytesFactory(key);
        }

        /**
         * データレコードを返却するファクトリクラス。
         */
        private static class BytesFactory extends FactoryBase<CyclicIterator<byte[]>> {

            /**
             * コンストラクタ。
             * @param key キー
             */
            public BytesFactory(CacheKey key) {
                super(key);
            }

            /** {@inheritDoc} */
            @Override
            protected CyclicIterator<byte[]> getValueOf(CacheKey key) {
                Reader<byte[]> reader = new Reader<byte[]>(key) {
                    @Override
                    byte[] readRecord(DataType dataType, String requestId) {
                        return support.getResponseMessageBinaryByRequestId(
                                dataType,
                                requestId);
                    }
                };
                List<byte[]> bytes = reader.readAll();
                return new CyclicIterator<byte[]>(bytes);
            }
        }
    }

    /**
     * SendSyncSupportを使用してデータを全件読み取るクラス。
     *
     * @param <T> 読み取るデータの型
     */
    abstract static class Reader<T> {

        /** キャッシュキー */
        private final CacheKey key;
        /**
         * コンストラクタ。
         * @param key キー
         */
        Reader(CacheKey key) {
            this.key = key;
        }

        /**
         * 実際にデータを読み取るクラス。
         * SendSyncSupportはインスタンス毎の状態を持たないので
         * 複数インスタンス生成しても問題ない。
         */
        protected final SendSyncSupport support = new SendSyncSupport();  // SUPPRESS CHECKSTYLE サブクラスに対してカプセル化不要であるため

        /**
         * 1件データを読み取る。
         * @param dataRecord データレコード
         : @param requestId リクエストID
         * @return 読み取ったデータ
         */
        abstract T readRecord(DataType dataRecord, String requestId);

        /**
         * 全件読み取りを行う。
         *
         * @return 読み取ったデータ全件
         */
        List<T> readAll() {

            List<T> all = new ArrayList<T>();
            // 読み込みはデータ種類につき1回かぎりなのでsyncronizedでも性能に影響しない。
            synchronized (Reader.class) {
                while (true) {
                    try {
                        T record = readRecord(key.dataType, key.requestId);
                        all.add(record);
                    } catch (RuntimeException e) {
                        if (e.getMessage().contains("receive message did not exists")) {
                            break;  // 全件読み取り完了。
                        } else {
                            throw e;  // 予期しない例外
                        }
                    }
                }
            }
            return all;
        }

    }

    /**
     * データ読み取り箇所を先頭に戻す。
     * （テスト用）
     */
    public void reset() {
        for (CyclicIterator itr : records.values()) {
            itr.reset();
        }
        for (CyclicIterator itr : bytesMap.values()) {
            itr.reset();
        }
    }
}

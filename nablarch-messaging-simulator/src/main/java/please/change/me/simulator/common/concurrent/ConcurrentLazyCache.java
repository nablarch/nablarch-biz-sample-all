package please.change.me.simulator.common.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 初回get時に値を取得し、取得した値をキャッシュするクラス。
 * 本クラスはスレッドセーフである。
 *
 * @param <K> キーの型
 * @param <V> 値の型
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class ConcurrentLazyCache<K, V> {

    /** キャッシュキーと、キーから値を取得するファクトリのペア */
    private final ConcurrentMap<K, ValueFactory<V>> container;

    /** ファクトリを生成するクラス。 */
    private final ValueFactoryBuilder<K, V> builder;

    /**
     * コンストラクタ
     *
     * @param builder ファクトリ生成クラス
     */
    public ConcurrentLazyCache(ValueFactoryBuilder<K, V> builder) {
        this.builder = builder;
        this.container = new ConcurrentHashMap<K, ValueFactory<V>>();
    }

    /**
     * 現在キャッシュしている値を全て取得する。
     * （テスト用）
     *
     * @return キャッシュしている値
     */
    public List<V> values() {
        List<V> values = new ArrayList<V>(container.size());
        for (ValueFactory<V> e : container.values()) {
            values.add(e.getValue());
        }
        return values;
    }

    /**
     * キーに対応する値を取得する。
     *
     * @param key キー
     * @return 値
     */
    public V get(K key) {
        ValueFactory<V> current = container.get(key);
        if (current == null) {

            // 初回get時、新規ファクトリインスタンスを生成
            // この処理は複数スレッドから実行される可能性がある。
            ValueFactory<V> newFactory = builder.newInstance(key);

            // 初回のみputされる。
            // 複数スレッドから同時にアクセスされても2回目以降はputされない。
            // その場合、ファクトリインスタンスは無駄になるが、同期しないので並列実行効率が上がる。
            current = container.putIfAbsent(key, newFactory);   // atomic!
            boolean putSuccess = (current == null);  // 2回目以降はfalseになる
            if (putSuccess) {
                current = newFactory;  // putが成功した場合（最初に到達したスレッド）は新しいファクトリで値を取得。
            }
        }
        return current.getValue(); // キーと1対1のファクトリから値を取得
    }

    /**
     * {@link ValueFactory}を生成するインタフェース。
     *
     * @param <K> キーの型
     * @param <V> 値の型
     */
    public interface ValueFactoryBuilder<K, V> {

        /**
         * 新しいファクトリインスタンスを取得する。
         *
         * @param key キー
         * @return ファクトリインスタンス
         */
        ValueFactory<V> newInstance(K key);
    }

    /**
     * 値を生成するクラス。
     * @param <V> 生成する値の型
     */
    public interface ValueFactory<V> {

        /**
         * 自身のキーに対応する値を取得する。
         *
         * @return 値
         */
        V getValue();
    }

    /**
     * キーから値を生成するファクトリクラス。
     * キャッシュ機能を持つ。
     * キーに使用するクラスが（{@link Object#equals(Object)}を正しくオーバーライドしている場合、
     * 値の取得処理（{@link #getValueOf(Object)}）が一度しか呼ばれないことが保証される。
     */
    public abstract static class CachingValueFactory<K, V> implements ValueFactory<V> {

        /** キー */
        private final K key;

        /**
         *  値（キャッシュ）。
         *  マルチスレッド下での動作を保証するためvolatile指定。
         */
        private volatile V memo;

        /**
         * 本インスタンスが担当するキー{@link #key}に対して
         * 1インスタンスになるロックオブジェクト。
         */
        private final Object lock = new Object();

        /**
         * コンストラクタ。
         *
         * @param key キー
         */
        protected CachingValueFactory(K key) {
            this.key = key;
        }


        /**
         * キーに対応する値を取得する。
         * 本メソッドをオーバーライドする場合、nullを返却してはならない。
         * 本メソッドはマルチスレッド下であっても、
         * キーに対して1回しか実行されないことが保証されている。
         *
         * @param key キー
         * @return キーに対応する値（nullでない）
         */
        protected abstract V getValueOf(K key);

        /**
         * 本インスタンスのキーに対応する値を取得する。
         *
         * @return 値
         */
        @Override
        public V getValue() {
            V value = memo;
            if (value != null) {   // キャッシュに値がある場合はそれを返却する。
                return value;
            }
            // 1種類のキーに対して本インスタンスは１つなので、ロックインスタンスも１つ
            // ConcurrentLazyCache#get()の時点でキーに対して単一のインスタンスが返却されているため
            synchronized (lock) {
                if (memo != null) {
                    return memo;    // synchronizedに到達した2番め以降のスレッドはここでreturnする。
                }

                // ここからの処理は最初にsyncronizedに到達したスレッドだけが実行する。
                value = getValueOf(key);
                if (value == null) {
                    throw new IllegalStateException(
                            "ValueFactory#getValueOf(K) must not return null. key=[" + key + "]");
                }
                memo = value;  // 最初のスレッドのみ1回値を設定する。
            }
            return value;
        }
    }
}

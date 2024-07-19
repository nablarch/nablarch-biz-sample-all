package please.change.me.simulator.common;

import nablarch.test.core.reader.DataType;

/**
 * キャッシュのキーになるクラス。
 * データ型とリクエストIDの組み合わせでデータの一意性を表す。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
class CacheKey {

    /** データ型 */
    final DataType dataType;  // SUPPRESS CHECKSTYLE  不変オブジェクトであり、カプセル化不要

    /** リクエストID*/
    final String requestId;    // SUPPRESS CHECKSTYLE 不変オブジェクトであり、カプセル化不要


    /** データ型とリクエストIDを組み合わせたキーの文字列表現 */
    final String key;          // SUPPRESS CHECKSTYLE 不変オブジェクトであり、カプセル化不要

    /**
     * コンストラクタ。
     * @param dataType データ型
     * @param requestId リクエストID
     */
    CacheKey(DataType dataType, String requestId) {
        this.dataType = dataType;
        this.requestId = requestId;
        this.key = dataType + "_" + requestId;
    }

    /**
     * 文字列表現を返却する。
     * @return データ型_リクエストID
     */
    @Override
    public String toString() {
        return key;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CacheKey cacheKey = (CacheKey) o;

        return key.equals(cacheKey.key);

    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return key.hashCode();
    }


}

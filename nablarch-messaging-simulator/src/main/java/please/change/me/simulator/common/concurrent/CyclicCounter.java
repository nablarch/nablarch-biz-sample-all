package please.change.me.simulator.common.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 繰り返しカウントアップできるカウンター
 * 本クラスはスレッドセーフである。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class CyclicCounter {

    /**
     * 内部カウンタ。
     * スレッドセーフにするため{@link AtomicInteger}を使用する。
     */
    private final AtomicInteger cnt;

    /**
     * カウンタ最大値。
     * 内部カウンタが、このカウンタ最大値までカウントアップすると0に戻る。
     */
    private final int max;

    /**
     * コンストラクタ。
     *
     * @param max カウンタ最大値
     */
    public CyclicCounter(int max) {
        this.max = max;
        cnt = new AtomicInteger(0);
    }

    /**
     * カウンタの値を取得し、カウンタの値を１増分する。
     *
     * @return 現在のカウンタの値。
     */
    public int getAndIncrement() {
        while (true) {
            int current = cnt.get();                      // 現在のカウンタ値
            int next = (current < max) ? current + 1 : 0; // 次のカウンタ値（最大値を超えた場合は0に戻す）
            if (cnt.compareAndSet(current, next)) {       // 現在のカウンタ値に変更がない場合のみ
                return current;                           // （他のスレッドから割り込み更新されていない）
            }                                             // 設定が成功する。
            // 設定に失敗した場合（割り込み有り）は
            // 番号を取り直して再試行する。
        }
    }

    /** カウンタを0にリセットする。*/
    public void reset() {
        cnt.set(0);
    }

}

package please.change.me.simulator.common;

import java.util.TreeMap;

/**
 * 出現回数を数えるカウンタ。
 *
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class FrequencyCounter<K> extends TreeMap<K, Integer> {
    /**
     * カウントアップする。
     * @param key カウント対象のキー
     */
    public void count(K key) {
        Integer cnt = get(key);
        if (cnt == null) {
            cnt = 0;
        }
        cnt += 1;
        put(key, cnt);
    }
}

package please.change.me.simulator.common.concurrent;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 繰り返し読み出し可能な{@link Iterator}実装クラス。
 * 本クラスはスレッドセーフである。
 *
 * @param <T> 内包するオブジェクトの型
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class CyclicIterator<T> implements Iterator<T> {

    /** 内容 */
    private final List<T> contents;

    /** インデックス */
    private final CyclicCounter index;

    /**
     * コンストラクタ。
     * @param contents 内包するオブジェクト
     */
    public CyclicIterator(List<T> contents) {
        this.contents = Collections.unmodifiableList(contents);
        int lastIndex = contents.size() - 1;
        this.index = new CyclicCounter(lastIndex);
    }

    /**
     * {@inheritDoc}
     * 常に真を返却する。
     */
    @Override
    public boolean hasNext() {
        return true;
    }

    /**
     * {@inheritDoc}
     * 元のListが空でない限り、繰り返し同じ内容を返却する。
     */
    @Override
    public T next() {
        if (contents.isEmpty()) {
            return null;
        }
        int nextIndex = index.getAndIncrement();
        return contents.get(nextIndex);
    }

    /**
     * {@inheritDoc}
     * この操作はサポートされない。
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * カウントをリセットする。
     * 再度、Listの先頭から読み出しが開始する。
     */
    public void reset() {
        index.reset();
    }

}

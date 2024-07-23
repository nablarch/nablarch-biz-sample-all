package please.change.me.simulator.outgoing;

import nablarch.core.dataformat.DataRecord;
import nablarch.fw.DataReader;
import nablarch.fw.ExecutionContext;
import please.change.me.simulator.common.concurrent.CyclicIterator;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 繰り返し同じデータを読み込む{@link DataReader}実装クラス。
 *
 * 本クラスはスレッドである。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class CyclicDataReader implements DataReader<DataRecord> {

    /** 繰り返し読み込み可能なイテレータ */
    private final CyclicIterator<DataRecord> itr;

    /** 現在の読み込み数 */
    private AtomicInteger readCnt = new AtomicInteger(0);

    /** 読み込み最大上限数 */
    private final int limit;

    /**
     * コンストラクタ。
     * @param src 入力元のデータレコード
     * @param repeatLimitCnt 繰り返し上限回数
     */
    public CyclicDataReader(List<DataRecord> src, int repeatLimitCnt) {
        this.itr = new CyclicIterator<DataRecord>(src);
        this.limit = repeatLimitCnt;
    }

    /** {@inheritDoc} */
    @Override
    public DataRecord read(ExecutionContext ctx) {
        int cnt = readCnt.incrementAndGet();
        return cnt <= limit ? itr.next() : null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext(ExecutionContext ctx) {
        return readCnt.get() < limit;
    }

    /** {@inheritDoc} */
    @Override
    public void close(ExecutionContext ctx) {
        itr.reset();
    }
}

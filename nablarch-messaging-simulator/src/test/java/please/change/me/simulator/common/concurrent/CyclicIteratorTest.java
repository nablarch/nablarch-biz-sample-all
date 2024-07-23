package please.change.me.simulator.common.concurrent;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link CyclicIterator}のテストクラス。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class CyclicIteratorTest {
    private CyclicIterator<Integer> target = new CyclicIterator<Integer>(
            Arrays.asList(1,2,3));

    /** nextのテスト */     
    @Test
    public void testNext() {

        assertThat(target.hasNext(), is(true));
        assertThat(target.next(), is(1)); // 1
        assertThat(target.next(), is(2)); // 2
        assertThat(target.next(), is(3)); // 3

        assertThat(target.hasNext(), is(true));
        assertThat(target.next(), is(1)); // 4
        assertThat(target.next(), is(2)); // 5
        assertThat(target.next(), is(3)); // 6
        assertThat(target.hasNext(), is(true));

    }

    /** removeを起動すると例外が発生すること。*/
    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        target.remove();
    }

}

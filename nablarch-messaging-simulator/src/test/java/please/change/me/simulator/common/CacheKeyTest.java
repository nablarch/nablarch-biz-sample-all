package please.change.me.simulator.common;


import nablarch.test.core.reader.DataType;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link CacheKey}のテストクラス。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class CacheKeyTest {


    @Test
    public void testEqualsSame() {
        CacheKey one = new CacheKey(DataType.MESSAGE, "id");
        assertThat(one.equals(one), is(true));
    }

    @Test
    public void testEqualsNotSameClass() {
        CacheKey one = new CacheKey(DataType.MESSAGE, "id");
        String other = "not equal..";
        assertThat(one.equals(other), is(false));
    }

    @Test
    public void testEqualsNull() {
        CacheKey one = new CacheKey(DataType.MESSAGE, "id");
        String other = null;
        assertThat(one.equals(other), is(false));
    }

    @Test
    public void testEquals() {
        CacheKey one = new CacheKey(DataType.MESSAGE, "id");
        CacheKey other = new CacheKey(DataType.MESSAGE, "id");
        assertThat(one.equals(other), is(true));
    }

    @Test
    public void testToString() {
        CacheKey key = new CacheKey(DataType.MESSAGE, "id");
        assertThat(key.toString(), is("MESSAGE_id"));
    }
}
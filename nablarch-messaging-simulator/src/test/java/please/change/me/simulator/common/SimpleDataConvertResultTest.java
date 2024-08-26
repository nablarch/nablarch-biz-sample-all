package please.change.me.simulator.common;


import org.junit.Test;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link SimpleDataConvertResult}のテスト。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class SimpleDataConvertResultTest {

    private Map<String, ?> resultMap = new HashMap<>();

    @Test
    public void testAccessor() {
        SimpleDataConvertResult target = new SimpleDataConvertResult();
        target.setCharset(Charset.forName("EUC-JP"));
        target.setDataType("dataType");
        target.setMimeType("mimeType");
        target.setResultMap(resultMap);
        target.setResultText("resultText");

        assertThat(target.getCharset(), is(Charset.forName("EUC-JP")));
        assertThat(target.getDataType(), is("dataType"));
        assertThat(target.getMimeType(), is("mimeType"));
        assertThat(target.getResultMap() == resultMap, is(true));
        assertThat(target.getResultText(), is("resultText"));
    }
}
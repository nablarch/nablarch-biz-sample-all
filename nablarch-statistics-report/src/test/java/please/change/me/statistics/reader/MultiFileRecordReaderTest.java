package please.change.me.statistics.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import nablarch.core.dataformat.DataRecord;
import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.FilePathSetting;
import nablarch.fw.ExecutionContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link MultiFileRecordReader}のテストクラス。
 *
 * @author hisaaki sioiri
 */
public class MultiFileRecordReaderTest {

    @BeforeClass
    public static void setupClass() {
        final FilePathSetting filePathSetting = new FilePathSetting();
        HashMap<String, String> basePathSettings = new HashMap<String, String>();
        basePathSettings.put("input", "classpath:please/change/me/statistics/reader/data");
        basePathSettings.put("format", "classpath:please/change/me/statistics/reader/data");
        filePathSetting.setBasePathSettings(basePathSettings);

        HashMap<String, String> extensions = new HashMap<String, String>();
        extensions.put("format", "fmt");
        filePathSetting.setFileExtensions(extensions);
        SystemRepository.load(new ObjectLoader() {
            public Map<String, Object> load() {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("filePathSetting", filePathSetting);
                return data;
            }
        });
    }

    @AfterClass
    public static void afterClass() {
        SystemRepository.clear();
    }

    /** 読み込み対象のファイルを指定しなかった場合 */
    @Test
    public void testUnsetFiles() {

        ExecutionContext context = new ExecutionContext();
        MultiFileRecordReader reader = new MultiFileRecordReader();

        reader.setLayoutFile("layout1");

        try {
            assertThat(reader.hasNext(context), is(false));
            fail("ここはとおらない。");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("data file name was blank. data file name must not be blank."));
        }
    }

    /** 読み込み対象のファイルを指定しなかった(サイズ0のListを指定)場合 */
    @Test
    public void testZeroFile() {

        ExecutionContext context = new ExecutionContext();
        MultiFileRecordReader reader = new MultiFileRecordReader();

        List<String> fileSet = new ArrayList<String>();
        reader.setFileList(fileSet);
        reader.setLayoutFile("format", "layout1");

        try {
            assertThat(reader.hasNext(context), is(false));
            fail("ここはとおらない。");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("data file name was blank. data file name must not be blank."));
        }
    }

    /** 読み込み対象のファイルが1ファイルだけの場合。 */
    @Test
    public void testSingleFile() {

        ExecutionContext context = new ExecutionContext();
        MultiFileRecordReader reader = new MultiFileRecordReader();

        List<String> fileSet = new ArrayList<String>();
        fileSet.add("test1.csv");
        reader.setFileList(fileSet);
        reader.setLayoutFile("layout1");

        assertThat(reader.hasNext(context), is(true));

        DataRecord data1 = reader.read(context);
        assertThat(data1, is(notNullValue()));
        assertThat(data1.getString("item1"), is("1"));
        assertThat(data1.getString("item2"), is("2"));
        assertThat(data1.getString("item3"), is("3"));

        assertThat(reader.hasNext(context), is(true));
        DataRecord data2 = reader.read(context);
        assertThat(data2, is(notNullValue()));
        assertThat(data2.getString("item1"), is("4"));
        assertThat(data2.getString("item2"), is("5"));
        assertThat(data2.getString("item3"), is("6"));

        // 3レコード目は存在しない
        assertThat(reader.hasNext(context), is(false));
        assertThat(reader.read(context), is(nullValue()));

        reader.close(context);
    }

    /**
     * 複数ファイルを指定した場合。
     * <p/>
     * {@link MultiFileRecordReader#hasNext(nablarch.fw.ExecutionContext)}に着目したテスト
     */
    @Test
    public void testMultiFileHasNext() {
        ExecutionContext context = new ExecutionContext();
        MultiFileRecordReader reader = new MultiFileRecordReader();

        List<String> fileSet = new ArrayList<String>();
        fileSet.add("test2.csv");
        fileSet.add("test1.csv");
        reader.setFileList("input", fileSet);
        reader.setLayoutFile("format", "layout1");

        assertThat(reader.hasNext(context), is(true));
        reader.read(context);

        assertThat(reader.hasNext(context), is(true));
        reader.read(context);

        assertThat(reader.hasNext(context), is(true));
        reader.read(context);

        assertThat(reader.hasNext(context), is(true));
        reader.read(context);

        assertThat(reader.hasNext(context), is(true));
        reader.read(context);

        // 6レコード目は存在しない
        assertThat(reader.hasNext(context), is(false));
        assertThat(reader.read(context), is(nullValue()));
    }

    /**
     * 複数ファイルを指定した場合。
     * {@link MultiFileRecordReader#read(nablarch.fw.ExecutionContext)}に着目したテスト。
     */
    @Test
    public void testMultiFileRead() {
        ExecutionContext context = new ExecutionContext();
        MultiFileRecordReader reader = new MultiFileRecordReader();

        List<String> fileSet = new ArrayList<String>();
        fileSet.add("test2.csv");
        fileSet.add("test1.csv");
        reader.setFileList(fileSet);
        reader.setLayoutFile("layout1");

        DataRecord data1 = reader.read(context);
        assertThat(data1, is(notNullValue()));
        assertThat(data1.getString("item1"), is("1"));
        assertThat(data1.getString("item2"), is("2"));
        assertThat(data1.getString("item3"), is("3"));

        DataRecord data2 = reader.read(context);
        assertThat(data2, is(notNullValue()));
        assertThat(data2.getString("item1"), is("4"));
        assertThat(data2.getString("item2"), is("5"));
        assertThat(data2.getString("item3"), is("6"));

        DataRecord data3 = reader.read(context);
        assertThat(data3, is(notNullValue()));
        assertThat(data3.getString("item1"), is("7"));
        assertThat(data3.getString("item2"), is("8"));
        assertThat(data3.getString("item3"), is("9"));

        DataRecord data4 = reader.read(context);
        assertThat(data4, is(notNullValue()));
        assertThat(data4.getString("item1"), is("1"));
        assertThat(data4.getString("item2"), is("2"));
        assertThat(data4.getString("item3"), is("3"));

        DataRecord data5 = reader.read(context);
        assertThat(data5, is(notNullValue()));
        assertThat(data5.getString("item1"), is("4"));
        assertThat(data5.getString("item2"), is("5"));
        assertThat(data5.getString("item3"), is("6"));

        // 6レコード目は存在しない
        assertThat(reader.read(context), is(nullValue()));
    }
}



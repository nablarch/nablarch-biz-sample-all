/**
 * 
 */
package please.change.me.core.dataformat;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nablarch.core.dataformat.DataRecord;
import nablarch.core.dataformat.DataRecordFormatter;
import nablarch.core.dataformat.FieldDefinition;
import nablarch.core.dataformat.FormatterFactory;
import nablarch.core.dataformat.InvalidDataFormatException;
import nablarch.core.dataformat.LayoutDefinition;
import nablarch.core.dataformat.LayoutFileParser;
import nablarch.core.dataformat.convertor.value.ValueConvertor;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.ComponentDefinitionLoader;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.util.Builder;
import nablarch.core.util.FilePathSetting;
import nablarch.core.util.FileUtil;
import nablarch.test.support.SystemRepositoryResource;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * {@link FormUrlEncodedDataRecordFormatter}のテストを行います。
 * 
 * @author TIS
 */
public class FormUrlEncodedDataRecordFormatterTest {

    @Rule
    public TestName testNameRule = new TestName();

    @ClassRule
    public static final SystemRepositoryResource RESOURCE = new SystemRepositoryResource(
            "please/change/me/core/dataformat/FormUrlEncodedDataRecordFormatterTest.xml");

    /**
     * フォーマット定義ファイル名を取得します
     * @return
     */
    protected String getFormatFileName() {
        FilePathSetting fps = FilePathSetting.getInstance()
                .addBasePathSetting("format", "file:tmp")
                .addFileExtensions("fortmat", "fmt");
        return Builder.concat(
                   fps.getBasePathSettings().get("format").getPath(),
                   "/", testNameRule.getMethodName(), ".", 
                   fps.getFileExtensions().get("format")
               );
        
    }

    /**
     * フォーマッターを作成します。
     * @return FormUrlEncodedのフォーマッター
     */
    protected DataRecordFormatter createFormatter(InputStream is) {
        return createFormatter(is, true);
    }
    
    /**
     * フォーマッターを作成します。
     * @return FormUrlEncodedのフォーマッター
     */
    protected DataRecordFormatter createFormatter(OutputStream os) {
        return createFormatter(os, true);
    }
    
    /**
     * フォーマッターを作成します。
     * @return FormUrlEncodedのフォーマッター
     */
    protected DataRecordFormatter createFormatter(InputStream is, boolean isInitialize) {
        LayoutDefinition ld = new LayoutFileParser(getFormatFileName()).parse();
        DataRecordFormatter formatter = FormatterFactory.getInstance().createFormatter(ld);
        formatter.setDefinition(ld);
        formatter.setInputStream(is);
        if(isInitialize) {
            formatter.initialize();
        }
        return formatter;
    }
    
    /**
     * フォーマッターを作成します。
     * @return FormUrlEncodedのフォーマッター
     */
    protected DataRecordFormatter createFormatter(OutputStream os, boolean isInitialize) {
        LayoutDefinition ld = new LayoutFileParser(getFormatFileName()).parse();
        DataRecordFormatter formatter = FormatterFactory.getInstance().createFormatter(ld);
        formatter.setDefinition(ld);
        formatter.setOutputStream(os);
        if(isInitialize) {
            formatter.initialize();
        }
        return formatter;
    }
    
    /**
     * マップの検証を行います。
     * @param expected 期待結果
     * @param actual 実行結果
     */
    protected void assertMap(Map<String, ?> expected, Map<String, ?> actual) {
        if (expected != null) {
            for(String key : expected.keySet()) {
                if (expected.get(key) instanceof String[]) {
                    assertArrayEquals("Error Key:[" + key + "]", (String[])expected.get(key), (String[])actual.get(key));
                } else {
                    assertEquals("Error Key:[" + key + "]", expected.get(key), actual.get(key));
                }
            }
        }
    }
    
    private File createTempFile(String filename, List<String> data) throws IOException {
        File file = new File(filename);
        file.deleteOnExit();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        try {
            for (String str : data) {
                bw.write(str);
                bw.newLine();
            }
        } finally {
            FileUtil.closeQuietly(bw);
        }
        return file;
    }
    
    /**
     * 複数回フォーマッタの初期化処理を行う。<br>
     */
    @Test
    public void testInitializeFomatterDupl() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 X");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{});
        DataRecordFormatter readFormatter = createFormatter(bais);
        readFormatter.initialize().initialize();
        readFormatter.close();
        
        // 書き込みテスト
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataRecordFormatter writeFormatter = createFormatter(baos);
        writeFormatter.initialize().initialize();
        writeFormatter.close();
    }
    
    /**
     * 読み込みテストを行います。
     * @param targetStr 読み込み対象文字列
     * @param expectedMap 期待結果
     * @throws IOException 入出力に伴うエラーが発生した場合
     */
    private void readTest(String targetStr, Map<String, Object> expectedMap) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(targetStr.getBytes("UTF-8"));
        DataRecordFormatter readFormatter = createFormatter(bais);
        DataRecord record = readFormatter.readRecord();
        readFormatter.close();
        assertMap(expectedMap, record);
    }

    /**
     * 書き込みテストを行います。
     * @param targetMap 書き込み対象マップ
     * @param expectedStr 期待結果
     * @throws IOException 入出力に伴うエラーが発生した場合
     */
    private void writeTest(Map<String, Object> targetMap, String expectedStr) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataRecordFormatter writeFormatter = createFormatter(baos);
        writeFormatter.writeRecord(targetMap);
        writeFormatter.close();
        assertEquals(expectedStr, baos.toString("UTF-8"));
    }
    
    /**
     * ASCII値のみの単項目の読み書きテストです。
     */
    @Test
    public void testNormalReadAndWrite() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 X");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=value1&key2=value2";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key2", "value2");
                }}
        ;
        
        readTest(targetStr, expectedMap);
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key2", "value2");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=value1&key2=value2";
        
        writeTest(targetMap, expectedStr);
    }
    

    /**
     * URLエンコーディングされた単項目の読み書きテストです。
     */
    @Test
    public void testURLEncoded() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 X");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=%e3%83%90%e3%83%aa%e3%83%a5%e3%83%bc%ef%bc%91&key2=%e3%83%90%e3%83%aa%e3%83%a5%e3%83%bc%ef%bc%92";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", "バリュー１");
                    put("key2", "バリュー２");
                }}
        ;
        
        readTest(targetStr, expectedMap);
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "バリュー１");
                    put("key2", "バリュー２");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=%E3%83%90%E3%83%AA%E3%83%A5%E3%83%BC%EF%BC%91&key2=%E3%83%90%E3%83%AA%E3%83%A5%E3%83%BC%EF%BC%92";
        
        writeTest(targetMap, expectedStr);
    }
    
    /**
     * 定義範囲内の配列の読み書きテストです。
     */
    @Test
    public void testArray() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 [1..2] X");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=value1-1&key1=value1-2&key2=value2";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", new String[]{"value1-1","value1-2"});
                    put("key2", "value2");
                }}
        ;
        
        readTest(targetStr, expectedMap);
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", new String[]{"value1-1","value1-2"});
                    put("key2", "value2");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=value1-1&key1=value1-2&key2=value2";
        
        writeTest(targetMap, expectedStr);
    }
    
    /**
     * 定義範囲より実際の要素数数が大きい配列の読み書きテストです。
     */
    @Test
    public void testLargeArray() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 [2..2] X");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=value1-1&key1=value1-2&key1=value1-3&key2=value2";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", new String[]{"value1-1","value1-2"});
                    put("key2", "value2");
                }}
        ;
        
        try {
            readTest(targetStr, expectedMap);
            
            fail();
        } catch( InvalidDataFormatException e) {
            assertTrue(e.getMessage().contains("key1 is out of range array"));
        }
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", new String[]{"value1-1","value1-2","value1-3"});
                    put("key2", "value2");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=value1-1&key1=value1-2&key2=value2";
        
        try {
            writeTest(targetMap, expectedStr);

            fail();
        } catch( InvalidDataFormatException e) {
            assertTrue(e.getMessage().contains("key1 is out of range array"));
        }
    }
    
    /**
     * 定義範囲より実際の要素数数が小さい配列の読み書きテストです。
     */
    @Test
    public void testSmallArray() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 [3..3] X");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=value1-1&key1=value1-2&key2=value2";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", new String[]{"value1-1","value1-2","value1-3"});
                    put("key2", "value2");
                }}
        ;
        
        try {
            readTest(targetStr, expectedMap);
            
            fail();
        } catch( InvalidDataFormatException e) {
            assertTrue(e.getMessage().contains("key1 is out of range array"));
        }
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", new String[]{"value1-1","value1-2"});
                    put("key2", "value2");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=value1-1&key1=value1-2&key1=value1-3&key2=value2";
        
        try {
            writeTest(targetMap, expectedStr);

            fail();
        } catch( InvalidDataFormatException e) {
            assertTrue(e.getMessage().contains("key1 is out of range array"));
        }
    }
    
    /**
     * 定義が配列で実際の値が単項目の読み書きテストです。
     */
    @Test
    public void testArrayButUnitItem() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 [3..3] X");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=value1-1&key2=value2";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", new String[]{"value1-1","value1-2","value1-3"});
                    put("key2", "value2");
                }}
        ;
        
        try {
            readTest(targetStr, expectedMap);
            
            fail();
        } catch( InvalidDataFormatException e) {
            assertTrue(e.getMessage().contains("key1 is out of range array"));
        }
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key2", "value2");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=value1-1&key1=value1-2&key1=value1-3&key2=value2";
        
        try {
            writeTest(targetMap, expectedStr);

            fail();
        } catch( InvalidDataFormatException e) {
            assertTrue(e.getMessage().contains("key1 is out of range array"));
        }
    }

    /**
     * 定義が配列でなく実際の値が配列の読み書きテストです。
     */
    @Test
    public void testUnitItemButArray() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 X");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=value1-1&key1=value1-2&key2=value2";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key2", "value2");
                }}
        ;
        
        try {
            readTest(targetStr, expectedMap);
            
            fail();
        } catch( InvalidDataFormatException e) {
            assertTrue(e.getMessage().contains("key1 is not array but many keys"));
        }
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", new String[]{"value1-1", "value1-2"});
                    put("key2", "value2");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=value1&key2=value2";
        
        try {
            writeTest(targetMap, expectedStr);

            fail();
        } catch( InvalidDataFormatException e) {
            assertTrue(e.getMessage().contains("key1 is not array but many keys"));
        }
    }

    /**
     * 必須項目で実際の値が格納されていない場合の読み書きテストです。
     */
    @Test
    public void testRequiredItemNone() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 X");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key2=value2";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key2", "value2");
                }}
        ;
        
        try {
            readTest(targetStr, expectedMap);
            
            fail();
        } catch( InvalidDataFormatException e) {
            assertTrue(e.getMessage().contains("key1 is required"));
        }
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key2", "value2");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=value1&key2=value2";
        
        try {
            writeTest(targetMap, expectedStr);

            fail();
        } catch( InvalidDataFormatException e) {
            assertTrue(e.getMessage().contains("key1 is required"));
        }
    }

    /**
     * 任意項目で実際の値が格納されていない場合の読み書きテストです。
     */
    @Test
    public void testNoRequiredItemNone() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 [*] X");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key2=value2";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key2", "value2");
                }}
        ;
        
        readTest(targetStr, expectedMap);
            
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key2", "value2");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key2=value2";
        
        writeTest(targetMap, expectedStr);
    }

    /**
     * 連続した任意項目で実際の値が格納されていない場合の読み書きテストです。
     */
    @Test
    public void testNoRequiredItemNoneMulti1() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 [*] X");
                add("2 key2 [*] X");
                add("3 key3 [*] X");
                add("4 key4 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key4=value4";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key4", "value4");
                }}
        ;
        
        readTest(targetStr, expectedMap);
            
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key4", "value4");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key4=value4";
        
        writeTest(targetMap, expectedStr);
    }

    /**
     * 連続した任意項目で実際の値が格納されていない場合の読み書きテストです。
     */
    @Test
    public void testNoRequiredItemNoneMulti2() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 X");
                add("2 key2 [*] X");
                add("3 key3 [*] X");
                add("4 key4 [*] X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=value1";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                }}
        ;
        
        readTest(targetStr, expectedMap);
        
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=value1";
        
        writeTest(targetMap, expectedStr);
    }
    
    /**
     * 連続した任意項目で実際の値が格納されていない場合の読み書きテストです。
     */
    @Test
    public void testNoRequiredItemNoneMulti3() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 X");
                add("2 key2 [*] X");
                add("3 key3 [*] X");
                add("4 key4 [*] X");
                add("5 key5 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=value1&key5=value5";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key5", "value5");
                }}
        ;
        
        readTest(targetStr, expectedMap);
        
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key5", "value5");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=value1&key5=value5";
        
        writeTest(targetMap, expectedStr);
    }
    
    /**
     * 連続した任意項目で実際の値が格納されていない場合の読み書きテストです。
     */
    @Test
    public void testNoRequiredItemNoneMulti4() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 X");
                add("2 key2 [*] X");
                add("3 key3 [*] X");
                add("4 key4 [*] X");
                add("5 key5 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=value1&key2=value2&key5=value5";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key2", new String[]{"value2"});
                    put("key5", "value5");
                }}
        ;
        
        readTest(targetStr, expectedMap);
        
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key2", new String[]{"value2"});
                    put("key5", "value5");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=value1&key2=value2&key5=value5";
        
        writeTest(targetMap, expectedStr);
    }
    
    /**
     * 連続した任意項目で実際の値が格納されていない場合の読み書きテストです。
     */
    @Test
    public void testNoRequiredItemNoneMulti5() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 X");
                add("2 key2 [*] X");
                add("3 key3 [*] X");
                add("4 key4 [*] X");
                add("5 key5 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=value1&key3=value3&key5=value5";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key3", new String[]{"value3"});
                    put("key5", "value5");
                }}
        ;
        
        readTest(targetStr, expectedMap);
        
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key3", new String[]{"value3"});
                    put("key5", "value5");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=value1&key3=value3&key5=value5";
        
        writeTest(targetMap, expectedStr);
    }
    
    /**
     * 連続した任意項目で実際の値が格納されていない場合の読み書きテストです。
     */
    @Test
    public void testNoRequiredItemNoneMulti6() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 X");
                add("2 key2 [*] X");
                add("3 key3 [*] X");
                add("4 key4 [*] X");
                add("5 key5 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=value1&key4=value4&key5=value5";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key4", new String[]{"value4"});
                    put("key5", "value5");
                }}
        ;
        
        readTest(targetStr, expectedMap);
        
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key4", new String[]{"value4"});
                    put("key5", "value5");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=value1&key4=value4&key5=value5";
        
        writeTest(targetMap, expectedStr);
    }
    
    /**
     * 最後の要素がキーのみの場合の読み書きテストです。
     */
    @Test
    public void testLastElementKeyOnly() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 X");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=value1&key2=";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key2", "");
                }}
        ;
        
        readTest(targetStr, expectedMap);
            
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key2", "");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=value1&key2=";
        
        writeTest(targetMap, expectedStr);
    }

    /**
     * 初期化前の読み書きテストです。
     */
    @Test
    public void testBeforeInitialize() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 X");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        try {
            DataRecordFormatter readFormatter = createFormatter((InputStream)null, false);
            assertFalse(readFormatter.hasNext());
            readFormatter.readRecord();
            fail();
        } catch( IllegalStateException e) {
            assertTrue(e.getMessage().contains("input stream was not set. input stream must be set before reading."));
        }
        
        // 書き込みテスト
        try {
            DataRecordFormatter writeFormatter = createFormatter((OutputStream)null, false);
            writeFormatter.writeRecord(null);
            fail();
        } catch( IllegalStateException e) {
            assertTrue(e.getMessage().contains("output stream was not set. output stream must be set before writing."));
        }
    }
    
    /**
     * HasNextのテストです。
     */
    @Test
    public void testHasNext() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 X");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=value1&key2=";
        
        ByteArrayInputStream bais = new ByteArrayInputStream(targetStr.getBytes("UTF-8"));
        DataRecordFormatter readFormatter = createFormatter(bais);
        
        // 読む前はtrue
        assertTrue(readFormatter.hasNext());
        
        readFormatter.readRecord();
        
        // 読んだ後はfalse
        assertFalse(readFormatter.hasNext());
    }


    /**
     * デフォルト値を設定した場合の読み書きテストです。
     */
    @Test
    public void testConverterDefaultValue() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 [0..1] X \"defval\"");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key2=value2";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key2", "value2");
                }}
        ;
        
        readTest(targetStr, expectedMap);
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key2", "value2");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=defval&key2=value2";
        
        writeTest(targetMap, expectedStr);
    }

    /**
     * 数値変換コンバータを設定した場合の読み書きテストです。
     */
    @Test
    public void testConverterNumber() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 X number");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=123&key2=value2";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", new BigDecimal(123));
                    put("key2", "value2");
                }}
        ;
        
        readTest(targetStr, expectedMap);
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", new BigDecimal(123));
                    put("key2", "value2");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=123&key2=value2";
        
        writeTest(targetMap, expectedStr);
    }
    
    /**
     * 符号付数値変換コンバータを設定した場合の読み書きテストです。
     */
    @Test
    public void testConverterSignedNumber() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 X signed_number");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=-123&key2=value2";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", new BigDecimal(-123));
                    put("key2", "value2");
                }}
        ;
        
        readTest(targetStr, expectedMap);
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", new BigDecimal(-123));
                    put("key2", "value2");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = 
                "key1=-123&key2=value2";
        
        writeTest(targetMap, expectedStr);
    }
    
    /**
     * コンバータでエラーが発生した場合の読み書きテストです。
     */
    @Test
    public void testConverterError() throws Exception {
        // テスト用のリポジトリ構築
        ComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "please/change/me/core/dataformat/convertor/ConvertorSetting.xml");
        DiContainer container = new DiContainer(loader);
        SystemRepository.clear();
        SystemRepository.load(container);
        
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 Test error");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=value1";
        
        try {
            readTest(targetStr, null);
            
            fail();
        } catch( InvalidDataFormatException e) {
            assertTrue(e.getMessage().contains("DummyException field name=[key1]"));
        }
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                }}
        ;
        
        try {
            writeTest(targetMap, null);

            fail();
        } catch( InvalidDataFormatException e) {
            assertTrue(e.getMessage().contains("DummyException field name=[key1]"));
        }
        
        // デフォルトのリポジトリに戻す
        loader = new XmlComponentDefinitionLoader(
                "please/change/me/core/dataformat/convertor/DefaultConvertorSetting.xml");
        container = new DiContainer(loader);
        SystemRepository.clear();
        SystemRepository.load(container);
    }

    @Test
    public void getMimeType() throws Exception {
        String result = new FormUrlEncodedDataRecordFormatter().getMimeType();
        assertThat("mime/typeの上書きがされていること", result, is("application/x-www-form-urlencoded"));
    }

    /**
     * テスト用に変換時にエラーを発生するコンバータ
     */
    public static class ErrorConverter implements ValueConvertor<Object, Object> {
        @Override
        public ValueConvertor<Object, Object> initialize(FieldDefinition field, Object... args) {
            return this;
        }
        @Override
        public Object convertOnRead(Object data) {
            throw new InvalidDataFormatException("DummyException");
        }
        @Override
        public Object convertOnWrite(Object data) {
            throw new InvalidDataFormatException("DummyException");
        }
    }
    
    /**
     * 同じフォーマッタから２回読み込みを行った場合のテストです。
     */
    @Test
    public void testReadTwice() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"FormUrlEncoded\"");
                add("text-encoding: \"UTF-8\"");
                add("[data]");
                add("1 key1 X");
                add("2 key2 X");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = 
                "key1=%e3%83%90%e3%83%aa%e3%83%a5%e3%83%bc%ef%bc%91&key2=%e3%83%90%e3%83%aa%e3%83%a5%e3%83%bc%ef%bc%92";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", "バリュー１");
                    put("key2", "バリュー２");
                }}
        ;
        
        ByteArrayInputStream bais = new ByteArrayInputStream(targetStr.getBytes("UTF-8"));
        DataRecordFormatter readFormatter = createFormatter(bais);
        DataRecord record = readFormatter.readRecord();
        assertMap(expectedMap, record);
        DataRecord record2 = readFormatter.readRecord();
        assertMap(null, record2); // ２回目はnull
        readFormatter.close();
    }
    
    /**
     * FormUrlEncodedDataFormatterFactoryを使用しても固定長データが読み書きできることを確認するテストです。
     */
    @Test
    public void testFixedDataReadWrite() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"Fixed\"");
                add("text-encoding: \"MS932\"");
                add("record-length: 12");
                add("[data]");
                add("1 key1 X(6)");
                add("7 key2 X(6)");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        String targetStr = "value1value2";
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key2", "value2");
                }}
        ;
        
        readTest(targetStr, expectedMap);
        
        // 書き込みテスト
        // 変換対象データ
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "value1");
                    put("key2", "value2");
                }}
        ;
        
        // 期待結果Map
        String expectedStr = "value1value2";
        
        writeTest(targetMap, expectedStr);
    }
    
}

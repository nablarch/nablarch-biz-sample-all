package please.change.me.core.dataformat.convertor.datatype;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
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
import nablarch.core.util.Builder;
import nablarch.core.util.FilePathSetting;
import nablarch.core.util.FileUtil;
import nablarch.test.support.SystemRepositoryResource;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * {@link EbcdicDoubleByteCharacterString}のテスト
 * 
 * @author TIS
 */
public class EbcdicDoubleByteCharacterStringTest {

    @Rule
    public TestName testNameRule = new TestName();

    @ClassRule
    public static final SystemRepositoryResource RESOURCE = new SystemRepositoryResource(
            "please/change/me/core/dataformat/convertor/datatype/ConvertorSetting.xml");

    @BeforeClass
    public static void setUpBeforeClass() {
        FormatterFactory.getInstance().setCacheLayoutFileDefinition(false);
    }
    
    /**
     * EBCDICバイト列を取得します
     * @param str 変換対象文字列
     * @param size 出力サイズ
     * @return EBCDICバイト列
     * @throws IOException
     */
    private byte[] getEbcdicBytes(String str, int size) throws IOException {
        byte[] bytes = str.getBytes("CP930");
        ByteBuffer buff = ByteBuffer.allocate(size);
        buff.put(bytes);
        byte[] pad = new byte[size - bytes.length];
        Arrays.fill(pad, (byte)0x40);
        buff.put(pad);
        return buff.array();
    }
    
    /**
     * データタイプを作成します
     * @param data データ
     * @return データタイプ
     */
    private EbcdicDoubleByteCharacterString createDataType(String data) {
        EbcdicDoubleByteCharacterString dataType = new EbcdicDoubleByteCharacterString();
        FieldDefinition fd = new FieldDefinition();
        fd.setEncoding(Charset.forName("CP930"));
        dataType.init(fd, data.length() * 2 + 2);
        return dataType;
    }
    
    /**
     * フォーマット定義ファイル名を取得します
     * @return
     */
    protected String getFormatFileName() {
        FilePathSetting fps = FilePathSetting.getInstance()
                .addBasePathSetting("format", "file:tmp")
                .addFileExtensions("format", "fmt");
        return Builder.concat(
                   fps.getBasePathSettings().get("format").getPath(),
                   "/", testNameRule.getMethodName(), ".", 
                   fps.getFileExtensions().get("format")
               );
        
    }

    /**
     * フォーマッターを作成します。
     * @return KeyValueのフォーマッター
     */
    protected DataRecordFormatter createFormatter(InputStream is) {
        LayoutDefinition ld = new LayoutFileParser(getFormatFileName()).parse();
        DataRecordFormatter formatter = FormatterFactory.getInstance().createFormatter(ld);
        formatter.setDefinition(ld);
        formatter.setInputStream(is);
        formatter.initialize();
        return formatter;
    }
    
    /**
     * フォーマッターを作成します。
     * @return KeyValueのフォーマッター
     */
    protected DataRecordFormatter createFormatter(OutputStream os) {
        LayoutDefinition ld = new LayoutFileParser(getFormatFileName()).parse();
        DataRecordFormatter formatter = FormatterFactory.getInstance().createFormatter(ld);
        formatter.setDefinition(ld);
        formatter.setOutputStream(os);
        formatter.initialize();
        return formatter;
    }
    
    /**
     * 指定されたデータの内容で一時ファイルを作成します
     * @param filename ファイル名
     * @param data 出力データ
     * @return 作成したファイル
     * @throws IOException ファイル出力に失敗した場合
     */
    private File createTempFile(String filename, List<String> data) throws IOException {
        File file = new File(filename);
        file.deleteOnExit();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
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
     * 読み込みテストを行います。
     * @param targetBytes 読み込み対象文字列
     * @param expectedMap 期待結果
     * @throws IOException 入出力に伴うエラーが発生した場合
     */
    private void readTest(byte[] targetBytes, Map<String, Object> expectedMap) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(targetBytes);
        DataRecordFormatter readFormatter = createFormatter(bais);
        DataRecord record = readFormatter.readRecord();
        readFormatter.close();
        assertEquals(expectedMap, record);
    }

    /**
     * 書き込みテストを行います。
     * @param targetMap 書き込み対象マップ
     * @param expectedBytes 期待結果
     * @throws IOException 入出力に伴うエラーが発生した場合
     */
    private void writeTest(Map<String, Object> targetMap, byte[] expectedBytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataRecordFormatter writeFormatter = createFormatter(baos);
        writeFormatter.writeRecord(targetMap);
        writeFormatter.close();
        assertArrayEquals(expectedBytes, baos.toByteArray());
    }
    /**
     * パディング文字のバイト長が4であることを確認する。
     */
    @Test
    public void testGetPaddingCharLength() {
        int paddingCharLength = new EbcdicDoubleByteCharacterString().getPaddingCharLength();
        assertEquals(4, paddingCharLength);
    }
    
    /**
     * データタイプ単体の読み取りテスト。<br>
     * 実データ長と定義データ長が等しい。
     */
    @Test
    public void testRead() throws Exception {
        String data = "あいうえお";
        String expected = "あいうえお";
        EbcdicDoubleByteCharacterString dataType = createDataType(data);
        String result = dataType.convertOnRead(data.getBytes("CP930"));
        assertEquals(expected, result);
    }

    /**
     * データタイプ単体の読み取りテスト。<br>
     * 実データ長が定義データ長より短い。
     */
    @Test
    public void testRead2() throws Exception {
        String data = "あいう";
        String expected = "あいう";
        EbcdicDoubleByteCharacterString dataType = createDataType(data);
        dataType.init(dataType.getField(), 12);
        String result = dataType.convertOnRead(data.getBytes("CP930"));
        assertEquals(expected, result);
    }
    
    /**
     * データタイプ単体の書き込みテスト。<br>
     * 実データ長と定義データ長が等しい。
     */
    @Test
    public void testWrite() throws Exception {
        String data = "あいうえお";
        byte[] expected = "あいうえお".getBytes("CP930");
        EbcdicDoubleByteCharacterString dataType = createDataType(data);
        byte[] result = dataType.convertOnWrite(data);
        assertArrayEquals(expected, result);
    }
    
    /**
     * データタイプ単体の書き込みテスト。<br>
     * 実データ長が定義データ長より短い。
     */
    @Test
    public void testWrite2() throws Exception {
        String data = "あいう";
        byte[] expected = "あいう　　".getBytes("CP930");
        EbcdicDoubleByteCharacterString dataType = createDataType(data);
        dataType.init(dataType.getField(), 12);
        byte[] result = dataType.convertOnWrite(data);
        assertArrayEquals(expected, result);
    }
    
    /**
     * フォーマット定義ファイルを用いた場合の読み書きテスト<br>
     * {@link EbcdicDoubleByteCharacterString}を使用した場合に、
     * 読み込み時、書き込み時ともにシフトコードがに対する操作が行われないことを確認する。
     */
    @Test
    public void testFormatDef() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"Fixed\"");
                add("text-encoding: \"CP930\"");
                add("record-length: 17");
                add("[data]");
                add("1 key1 X(5)");
                add("6 key2 ESN(12)");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        ByteBuffer targetBuff = ByteBuffer.allocate(17);
        targetBuff.put(getEbcdicBytes("abcde", 5));
        targetBuff.put(getEbcdicBytes("あいうえお", 12));
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", "abcde");
                    put("key2", "あいうえお");
                }}
        ;
        
        readTest(targetBuff.array(), expectedMap);

        // 書き込みテスト
        // 変換対象Map
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "abcde");
                    put("key2", "あいうえお");
                }}
        ;
        // 期待結果データ
        ByteBuffer expectedBuff = ByteBuffer.allocate(17);
        expectedBuff.put(getEbcdicBytes("abcde", 5));
        expectedBuff.put(getEbcdicBytes("あいうえお", 12));
        
        writeTest(targetMap, expectedBuff.array());
    }

    /**
     * 書き込み時にデフォルトのパディング文字でパディングが行われることを確認するテスト<br>
     */
    @Test
    public void testPadding() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"Fixed\"");
                add("text-encoding: \"CP930\"");
                add("record-length: 17");
                add("[data]");
                add("1 key1 X(5)");
                add("6 key2 ESN(12)");
            }
        });

        // 書き込みテスト
        // 変換対象Map
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "abcde");
                    put("key2", "あいう");
                }}
        ;
        // 期待結果データ
        ByteBuffer expectedBuff = ByteBuffer.allocate(17);
        expectedBuff.put(getEbcdicBytes("abcde", 5));
        expectedBuff.put(getEbcdicBytes("あいう　　", 12)); //全角スペースでパディングされる

        writeTest(targetMap, expectedBuff.array());
    }
    
    /**
     * 書き込み時に指定したパディング文字でパディングが行われることを確認するテスト<br>
     */
    @Test
    public void testPadding2() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"Fixed\"");
                add("text-encoding: \"CP930\"");
                add("record-length: 17");
                add("[data]");
                add("1 key1 X(5)");
                add("6 key2 ESN(12) pad(\"＿\")");
            }
        });
        
        // 書き込みテスト
        // 変換対象Map
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "abcde");
                    put("key2", "あいう");
                }}
        ;
        // 期待結果データ
        ByteBuffer expectedBuff = ByteBuffer.allocate(17);
        expectedBuff.put(getEbcdicBytes("abcde", 5));
        expectedBuff.put(getEbcdicBytes("あいう＿＿", 12)); //"＿"でパディングされる
        
        writeTest(targetMap, expectedBuff.array());
    }
    
    /**
     * 読み込み時に指定したパディング文字でトリムが行われることを確認するテスト<br>
     */
    @Test
    public void testTrim() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"Fixed\"");
                add("text-encoding: \"CP930\"");
                add("record-length: 17");
                add("[data]");
                add("1 key1 X(5)");
                add("6 key2 ESN(12)");
            }
        });

        // 読み込みテスト
        // 変換対象データ
        ByteBuffer targetBuff = ByteBuffer.allocate(17);
        targetBuff.put(getEbcdicBytes("abcde", 5));
        targetBuff.put(getEbcdicBytes("あいう　　", 12));
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", "abcde");
                    put("key2", "あいう");  // 全角スペースでトリムされる
                }}
        ;
        
        readTest(targetBuff.array(), expectedMap);
    }
    
    /**
     * 読み込み時に指定したパディング文字でトリムが行われることを確認するテスト<br>
     */
    @Test
    public void testTrim2() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"Fixed\"");
                add("text-encoding: \"CP930\"");
                add("record-length: 17");
                add("[data]");
                add("1 key1 X(5)");
                add("6 key2 ESN(12) pad(\"＿\")");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        ByteBuffer targetBuff = ByteBuffer.allocate(17);
        targetBuff.put(getEbcdicBytes("abcde", 5));
        targetBuff.put(getEbcdicBytes("あいう＿＿", 12));
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", "abcde");
                    put("key2", "あいう");  // "＿"でトリムされる
                }}
        ;
        
        readTest(targetBuff.array(), expectedMap);
    }

    /**
     * 半角文字を設定してしまった場合のテスト<br>
     */
    @Test
    public void testHalfchar() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"Fixed\"");
                add("text-encoding: \"CP930\"");
                add("record-length: 17");
                add("[data]");
                add("1 key1 X(5)");
                add("6 key2 ESN(12)");
            }
        });
        
        // 読み込みテスト
        // 変換対象データ
        ByteBuffer targetBuff = ByteBuffer.allocate(17);
        targetBuff.put(getEbcdicBytes("abcde", 5));
        targetBuff.put(getEbcdicBytes("0123456789", 12));
        
        // 期待結果Map
        Map<String, Object> expectedMap = 
                new HashMap<String, Object>() {{
                    put("key1", "abcde");
                    put("key2", "0123456789  "); // シフトコード操作がないのでパディングされて正常
                }}
        ;
        
        readTest(targetBuff.array(), expectedMap);

        // 書き込みテスト
        // 変換対象Map
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "abcde");
                    put("key2", "0123456789");
                }}
        ;
        // 期待結果データ
        ByteBuffer expectedBuff = ByteBuffer.allocate(17);
        expectedBuff.put(getEbcdicBytes("abcde", 5));
        expectedBuff.put(getEbcdicBytes("0123456789", 12));

        try {
            writeTest(targetMap, expectedBuff.array()); // シフトコードが見つからないので例外が発生する
            fail();
        } catch(InvalidDataFormatException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("cannot find shift out code. data:[0123456789]"));
        }
    }
    
    /**
     * 空文字の書き込みテスト<br>
     */
    @Test
    public void testWriteEmptyString() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"Fixed\"");
                add("text-encoding: \"CP930\"");
                add("record-length: 17");
                add("[data]");
                add("1 key1 X(5)");
                add("6 key2 ESN(12)");
            }
        });
        
        // 書き込みテスト
        // 変換対象Map
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "");
                    put("key2", "");
                }}
        ;
        // 期待結果データ
        ByteBuffer expectedBuff = ByteBuffer.allocate(17);
        expectedBuff.put(getEbcdicBytes("     ", 5));
        expectedBuff.put(getEbcdicBytes("　　　　　", 12));

        writeTest(targetMap, expectedBuff.array());
    }
    
    /**
     * nullの書き込みテスト<br>
     */
    @Test
    public void testWriteNullString() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"Fixed\"");
                add("text-encoding: \"CP930\"");
                add("record-length: 17");
                add("[data]");
                add("1 key1 X(5)");
                add("6 key2 ESN(12)");
            }
        });
        
        // 書き込みテスト
        // 変換対象Map
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "");
                    put("key2", null);
                }}
        ;
        // 期待結果データ
        ByteBuffer expectedBuff = ByteBuffer.allocate(17);
        expectedBuff.put(getEbcdicBytes("     ", 5));
        expectedBuff.put(getEbcdicBytes("　　　　　", 12));
        
        writeTest(targetMap, expectedBuff.array());
    }
    
    /**
     * 桁数超過の書き込みテスト<br>
     */
    @Test
    public void testWriteLargeString() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"Fixed\"");
                add("text-encoding: \"CP930\"");
                add("record-length: 17");
                add("[data]");
                add("1 key1 X(5)");
                add("6 key2 ESN(12)");
            }
        });
        
        // 書き込みテスト
        // 変換対象Map
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "abcde");
                    put("key2", "あいうえおかきくけこ");
                }}
        ;
        // 期待結果データ
        ByteBuffer expectedBuff = ByteBuffer.allocate(17);
        expectedBuff.put(getEbcdicBytes("abcde", 5));
        expectedBuff.put(getEbcdicBytes("あいうえお", 12));
        
        try {
            writeTest(targetMap, expectedBuff.array()); // シフトコードが見つからないので例外が発生する
            fail();
        } catch (InvalidDataFormatException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(
                    "invalid parameter was specified. too large data. field size = '12' data size = '22. data: あいうえおかきくけこ field name=[key2]."));
        }
    }
    
    /**
     * 全角半角混在データの書き込みテスト<br>
     */
    @Test
    public void testWriteHalfAndFullcharString() throws Exception {
        // フォーマット定義ファイル
        createTempFile(getFormatFileName(), new ArrayList<String>() {
            {
                add("file-type:     \"Fixed\"");
                add("text-encoding: \"CP930\"");
                add("record-length: 19");
                add("[data]");
                add("1 key1 X(5)");
                add("6 key2 ESN(14)");
            }
        });
        
        // 書き込みテスト
        // 変換対象Map
        Map<String, Object> targetMap = 
                new HashMap<String, Object>() {{
                    put("key1", "abcde");
                    put("key2", "あiうeお");
                }}
        ;
        // 期待結果データ
        ByteBuffer expectedBuff = ByteBuffer.allocate(21);
        expectedBuff.put(getEbcdicBytes("abcde", 5));
        expectedBuff.put(getEbcdicBytes("あiうeお", 16));
        
        try {
            writeTest(targetMap, expectedBuff.array()); // シフトコードが見つからないので例外が発生する
            fail();
        } catch (InvalidDataFormatException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("too many shift out code. data:[あiうeお]"));
        }
    }
    
}

package please.change.me.test.core.file;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

import nablarch.core.dataformat.DataRecord;
import nablarch.core.dataformat.FieldDefinition;
import nablarch.core.dataformat.LayoutDefinition;
import nablarch.core.dataformat.RecordDefinition;
import nablarch.core.dataformat.convertor.datatype.ByteStreamDataString;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link FormUrlEncodedTestDataConverter}のテストクラス
 * 
 * @author TIS
 */
public class FormUrlEncodedTestDataConverterTest {

    /**
     * レコード定義生成処理の正常系テスト<br>
     * 単一のDUMMYというフィールドが生成されていること、そのデータ長を確認する。
     */
    @Test
    public void testCreateDefinition() throws UnsupportedEncodingException {
        FormUrlEncodedTestDataConverter converter = new FormUrlEncodedTestDataConverter();
        LayoutDefinition defaultDefinition = createExcelLayoutDefinition();
        DataRecord currentData = createTestDataRecord();
        DataRecord convertedRecord = converter.convertData(defaultDefinition, currentData, Charset.forName("UTF-8"));

        LayoutDefinition createdDefinition = converter.createDefinition(defaultDefinition, convertedRecord, Charset.forName("UTF-8"));
        
        List<RecordDefinition> rdList = createdDefinition.getRecords();
        assertEquals(1, rdList.size());
        
        List<FieldDefinition> fdList = rdList.get(0).getFields();
        assertEquals(1, fdList.size());
        
        FieldDefinition fd = fdList.get(0);
        fd.setDataType(new ByteStreamDataString().initialize(fd.getConvertorSettingList().get("X")[0]));

        assertEquals("DUMMY", fd.getName());
        
        String urlEncodedTestData = createUrlEncodedTestData();

        assertEquals(urlEncodedTestData.length(), fd.getSize());
    }
    
    /**
     * レコード定義生成処理の異常系テスト<br>
     * currentDataがnullの場合
     */
    @Test
    public void testCreateDefinitionNullData() throws UnsupportedEncodingException {
        FormUrlEncodedTestDataConverter converter = new FormUrlEncodedTestDataConverter();
        LayoutDefinition defaultDefinition = createExcelLayoutDefinition();
        try {
            converter.createDefinition(defaultDefinition, null, Charset.forName("UTF-8"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("data is null"));
        }
        
    }
    
    /**
     * レコード定義生成処理の異常系テスト<br>
     * DUMMYカラムが含まれていない場合
     */
    @Test
    public void testCreateDefinitionNotContainsDUMMYCol() throws UnsupportedEncodingException {
        FormUrlEncodedTestDataConverter converter = new FormUrlEncodedTestDataConverter();
        LayoutDefinition defaultDefinition = createExcelLayoutDefinition();
        try {
            converter.createDefinition(defaultDefinition, new DataRecord(), Charset.forName("UTF-8"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("data is not contains dummy column. key=[DUMMY]"));
        }
        
    }
    
    /**
     * データ変換処理の正常系テスト<br>
     * 単一のDUMMYというフィールドにURLエンコードされた値が結合されて格納されていることを確認する。
     */
    @Test
    public void testConvertData() throws UnsupportedEncodingException {
        FormUrlEncodedTestDataConverter converter = new FormUrlEncodedTestDataConverter();
        LayoutDefinition defaultDefinition = createExcelLayoutDefinition();
        DataRecord currentData = createTestDataRecord();
        
        DataRecord convertedRecord = converter.convertData(defaultDefinition, currentData, Charset.forName("UTF-8"));
        
        String urlEncodedTestData = createUrlEncodedTestData();
        
        assertNotNull(convertedRecord.get("DUMMY"));
        assertEquals(urlEncodedTestData, convertedRecord.get("DUMMY"));
    }
    
    /**
     * データ変換処理の異常系テスト<br>
     * nullデータが含まれる場合
     */
    @Test
    public void testConvertDataNullData() throws UnsupportedEncodingException {
        FormUrlEncodedTestDataConverter converter = new FormUrlEncodedTestDataConverter();
        LayoutDefinition defaultDefinition = createExcelLayoutDefinition();
        DataRecord currentData = createTestDataRecord();
        currentData.put("漢字名", null);

        try {
            converter.convertData(defaultDefinition, currentData, Charset.forName("UTF-8"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("record contains not 'key=value' format data. col=漢字名"));
        }
        
    }
    
    /**
     * データ変換処理の異常系テスト<br>
     * key=valueではないデータが含まれる場合
     */
    @Test
    public void testConvertDataInvalidData() throws UnsupportedEncodingException {
        FormUrlEncodedTestDataConverter converter = new FormUrlEncodedTestDataConverter();
        LayoutDefinition defaultDefinition = createExcelLayoutDefinition();
        DataRecord currentData = createTestDataRecord();
        currentData.put("カナ名", "ヤマダタロウ");
        
        try {
            converter.convertData(defaultDefinition, currentData, Charset.forName("UTF-8"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("record contains not 'key=value' format data. col=カナ名"));
        }
        
    }
    
    /**
     * テストデータ作成処理
     * @return テストデータ
     */
    private DataRecord createTestDataRecord() {
        DataRecord currentData = new DataRecord();
        currentData.put("漢字名", "kanjiName=山田=太郎");
        currentData.put("カナ名", "kanaName=ヤマダ=タロウ");
        currentData.put("メール", "mailAddr=yamada.taro@mail.com");
        return currentData;
    }
    
    /**
     * URLエンコード済テストデータ作成処理
     * @return URLエンコード済テストデータ
     */
    private String createUrlEncodedTestData() throws UnsupportedEncodingException {
        return "kanjiName=" + URLEncoder.encode("山田=太郎", "UTF-8") + "&"
                + "kanaName=" + URLEncoder.encode("ヤマダ=タロウ", "UTF-8")+ "&"
                + "mailAddr=" + URLEncoder.encode("yamada.taro@mail.com", "UTF-8");
    }
    
    /**
     * エクセル定義から自動生成された想定のレイアウト定義を作成する。
     * @return エクセル定義から自動生成された想定のレイアウト定義
     */
    private LayoutDefinition createExcelLayoutDefinition() {
        // フィールド定義
        FieldDefinition fd1 = new FieldDefinition()
                            .setPosition(1)
                            .setName("漢字名")
                            .addConvertorSetting("X", new Object[]{100});

        FieldDefinition fd2 = new FieldDefinition()
                            .setPosition(101)
                            .setName("カナ名")
                            .addConvertorSetting("X", new Object[]{100});
        
        FieldDefinition fd3 = new FieldDefinition()
                            .setPosition(201)
                            .setName("メール")
                            .addConvertorSetting("X", new Object[]{100});
        
        // レコード定義
        RecordDefinition rd = new RecordDefinition()
                              .setTypeName("test")
                              .addField(fd1, fd2, fd3);
        
        // レイアウト定義
        LayoutDefinition ld = new LayoutDefinition();
        ld.addRecord(rd);
        ld.getDirective().put("file-type", "FormUrlEncoded");
        ld.getDirective().put("record-length", 300);

        return ld;
    }

}

package please.change.me.test.core.file;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.nio.charset.Charset;
import java.util.List;

import nablarch.core.dataformat.DataRecord;
import nablarch.core.dataformat.FieldDefinition;
import nablarch.core.dataformat.LayoutDefinition;
import nablarch.core.dataformat.RecordDefinition;
import nablarch.core.dataformat.convertor.datatype.ByteStreamDataString;

import org.junit.Test;

/**
 * {@link HierarchyStructureTestDataConverter}のテストクラス
 *
 * @author hisaaki sioiri
 */
public class HierarchyStructureTestDataConverterTest {

    /** テスト対象オブジェクト */
    private HierarchyStructureTestDataConverter sut = new HierarchyStructureTestDataConverter();

    /**
     * utf-8エンコーディングのデータの場合の
     * {@link HierarchyStructureTestDataConverter#createDefinition(LayoutDefinition, DataRecord, Charset)}のテスト
     * <p/>
     * カレントレコードの情報を元に、レイアウト定義のレコード長やポジションが上書きされること。
     */
    @Test
    public void testCreateDefinitionFromUtf8() throws Exception {

        //*********************************************************************
        // setup test data
        //*********************************************************************
        DataRecord record = createRecordData();

        //*********************************************************************
        // execute
        //*********************************************************************
        Charset encoding = Charset.forName("utf-8");
        LayoutDefinition definition = sut.createDefinition(
                createLayoutDefinition(encoding), record, encoding);

        //*********************************************************************
        // assert
        //*********************************************************************
        assertThat("レコード数は1", definition.getRecords().size(), is(1));

        RecordDefinition recordDefinition = definition.getRecordType("test-record");

        assertThat("レコード長は79", (Integer) definition.getDirective().get("record-length"), is(79));
        List<FieldDefinition> fields = recordDefinition.getFields();
        assertThat("フィールド数は3", fields.size(), is(3));

        //*********************************************************************
        // assert:field1
        //*********************************************************************
        FieldDefinition fieldDefinition1 = fields.get(0);
        assertThat(fieldDefinition1.getName(), is("data1"));
        assertThat(fieldDefinition1.getPosition(), is(1));
        assertThat(fieldDefinition1.getEncoding(), is(encoding));
        assertThat(fieldDefinition1.getConvertors().size(), is(0));
        assertThat(fieldDefinition1.getConvertorSettingList().get("X"), is(new Object[]{7}));

        //*********************************************************************
        // assert:field2
        //*********************************************************************
        FieldDefinition fieldDefinition2 = fields.get(1);
        assertThat(fieldDefinition2.getName(), is("data2"));
        assertThat(fieldDefinition2.getPosition(), is(8));
        assertThat(fieldDefinition2.getEncoding(), is(encoding));
        assertThat(fieldDefinition2.getConvertors().size(), is(0));
        assertThat(fieldDefinition2.getConvertorSettingList().get("XN"), is(new Object[]{64}));

        //*********************************************************************
        // assert:field3
        //*********************************************************************
        FieldDefinition fieldDefinition3 = fields.get(2);
        assertThat(fieldDefinition3.getName(), is("data3"));
        assertThat(fieldDefinition3.getPosition(), is(72));
        assertThat(fieldDefinition3.getEncoding(), is(encoding));
        assertThat(fieldDefinition3.getConvertors().size(), is(0));
        assertThat(fieldDefinition3.getConvertorSettingList().get("X"), is(new Object[]{8}));
    }

    /**
     * euc-jpエンコーディングのデータの場合の
     * {@link HierarchyStructureTestDataConverter#createDefinition(LayoutDefinition, DataRecord, Charset)}のテスト
     * <p/>
     * カレントレコードの情報を元に、レイアウト定義のレコード長やポジションが上書きされること。
     */
    @Test
    public void testCreateDefinitionFromEucJp() throws Exception {

        //*********************************************************************
        // setup test data
        //*********************************************************************
        DataRecord record = createRecordData();

        //*********************************************************************
        // execute
        //*********************************************************************
        Charset encoding = Charset.forName("euc-jp");
        LayoutDefinition definition = sut.createDefinition(
                createLayoutDefinition(encoding), record, encoding);

        //*********************************************************************
        // assert
        //*********************************************************************
        assertThat("レコード数は1", definition.getRecords().size(), is(1));
        assertThat("レコード長は69", (Integer) definition.getDirective().get("record-length"), is(69));

        RecordDefinition recordDefinition = definition.getRecordType("test-record");
        List<FieldDefinition> fields = recordDefinition.getFields();
        assertThat("フィールド数は3", fields.size(), is(3));

        //*********************************************************************
        // assert:field1
        //*********************************************************************
        FieldDefinition fieldDefinition1 = fields.get(0);
        assertThat(fieldDefinition1.getName(), is("data1"));
        assertThat(fieldDefinition1.getPosition(), is(1));
        assertThat(fieldDefinition1.getEncoding(), is(encoding));
        assertThat(fieldDefinition1.getConvertors().size(), is(0));
        assertThat(fieldDefinition1.getConvertorSettingList().get("X"), is(new Object[]{7}));

        //*********************************************************************
        // assert:field2
        //*********************************************************************
        FieldDefinition fieldDefinition2 = fields.get(1);
        assertThat(fieldDefinition2.getName(), is("data2"));
        assertThat(fieldDefinition2.getPosition(), is(8));
        assertThat(fieldDefinition2.getEncoding(), is(encoding));
        assertThat(fieldDefinition2.getConvertors().size(), is(0));
        assertThat(fieldDefinition2.getConvertorSettingList().get("XN"), is(new Object[]{54}));

        //*********************************************************************
        // assert:field3
        //*********************************************************************
        FieldDefinition fieldDefinition3 = fields.get(2);
        assertThat(fieldDefinition3.getName(), is("data3"));
        assertThat(fieldDefinition3.getPosition(), is(62));
        assertThat(fieldDefinition3.getEncoding(), is(encoding));
        assertThat(fieldDefinition3.getConvertors().size(), is(0));
        assertThat(fieldDefinition3.getConvertorSettingList().get("X"), is(new Object[]{8}));
    }

    /**
     * 複数回{@link please.change.me.test.core.file.HierarchyStructureTestDataConverter#createDefinition(LayoutDefinition, DataRecord, Charset)}
     * を呼び出した場合でも都度フィールド定義が更新されること
     */
    @Test
    public void testCreateDefinitionMultiCall() throws Exception {

        //*********************************************************************
        // setup test data
        //*********************************************************************
        DataRecord record = createRecordData();

        //*********************************************************************
        // execute:no1
        //*********************************************************************
        Charset encoding = Charset.forName("utf-8");
        LayoutDefinition definition = sut.createDefinition(
                createLayoutDefinition(encoding), record, encoding);

        //*********************************************************************
        // assert
        //*********************************************************************
        RecordDefinition recordDefinition = definition.getRecordType("test-record");
        List<FieldDefinition> fields = recordDefinition.getFields();
        assertThat("レコード長は79", (Integer) definition.getDirective().get("record-length"), is(79));
        assertThat("フィールド数は3", fields.size(), is(3));

        //*********************************************************************
        // assert:field2
        //*********************************************************************
        FieldDefinition fieldDefinition2 = fields.get(1);
        assertThat(fieldDefinition2.getName(), is("data2"));
        assertThat(fieldDefinition2.getPosition(), is(8));
        assertThat(fieldDefinition2.getEncoding(), is(encoding));
        assertThat(fieldDefinition2.getConvertors().size(), is(0));
        assertThat(fieldDefinition2.getConvertorSettingList().get("XN"), is(new Object[]{64}));

        //*********************************************************************
        // setup test data
        //*********************************************************************
        record = createRecordData();
        record.put("data2", "<name>あ</name><kanaName>イ</kanaName>");        // 40

        //*********************************************************************
        // execute:no2
        //*********************************************************************
        definition = sut.createDefinition(
                createLayoutDefinition(encoding), record, encoding);

        //*********************************************************************
        // assert
        //*********************************************************************
        recordDefinition = definition.getRecordType("test-record");
        assertThat("レコード長は55", (Integer) definition.getDirective().get("record-length"), is(55));
        fields = recordDefinition.getFields();
        assertThat("フィールド数は3", fields.size(), is(3));

        //*********************************************************************
        // assert:field2
        //*********************************************************************
        fieldDefinition2 = fields.get(1);
        assertThat(fieldDefinition2.getName(), is("data2"));
        assertThat(fieldDefinition2.getPosition(), is(8));
        assertThat(fieldDefinition2.getEncoding(), is(encoding));
        assertThat(fieldDefinition2.getConvertors().size(), is(0));
        assertThat(fieldDefinition2.getConvertorSettingList().get("XN"), is(new Object[]{40}));
    }

    /**
     * レイアウト定義にnullを指定した場合、例外が発生すること。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateDefinition_definitionNull() {
        sut.createDefinition(null, createRecordData(), Charset.defaultCharset());
    }

    /**
     * カレントレコードにnullを指定した場合、例外が発生すること。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateDefinition_currentRecordNull() {
        sut.createDefinition(createLayoutDefinition(Charset.defaultCharset()), null, Charset.defaultCharset());
    }

    /**
     * エンコーディング情報にnullを指定した場合、例外が発生すること。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateDefinition_charsetNull() {
        sut.createDefinition(createLayoutDefinition(Charset.defaultCharset()), createRecordData(), null);
    }

    /**
     * レイアウト定義のフィールド数とカレントレコードのフィールド長が異なる場合、例外が発生すること。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateDefinition_differentFieldSize() {
        DataRecord recordData = createRecordData();
        recordData.remove("data3");
        sut.createDefinition(
                createLayoutDefinition(Charset.defaultCharset()),
                recordData,
                Charset.defaultCharset());
    }

    /**
     * レイアウト定義内のフィールドがカレントレコード内に存在しない場合、例外が発生すること。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateDefinition_fieldNotFound() {
        DataRecord recordData = createRecordData();
        recordData.put("data4", recordData.get("data3"));
        recordData.remove("data3");
        sut.createDefinition(
                createLayoutDefinition(Charset.defaultCharset()),
                recordData,
                Charset.defaultCharset());
    }

    /**
     * 引数のレコード情報がそのまま返却されること。
     */
    @Test
    public void testConvertData() throws Exception {
        DataRecord param = createRecordData();
        DataRecord actual = sut.convertData(createLayoutDefinition(Charset.defaultCharset()), param,
                Charset.defaultCharset());

        assertThat(actual, is(param));
    }

    /**
     * レコード情報がnullの場合は、nullがそのまま返却されること。
     *
     * @throws Exception
     */
    @Test
    public void testConvertData_recordNull() throws Exception {
        DataRecord actual = sut.convertData(createLayoutDefinition(Charset.defaultCharset()), null,
                Charset.defaultCharset());

        assertThat(actual, is(nullValue()));
    }

    /**
     * テスト用のデータレコードを生成する。
     *
     * @return 生成したデータレコード
     */
    private static DataRecord createRecordData() {
        DataRecord record = new DataRecord();
        record.put("data1", "<users>");
        record.put("data2", "<name>あいうえお</name><kanaName>アイウエオ</kanaName>");
        record.put("data3", "</users>");
        return record;
    }

    /**
     * テストで使用するテストデータ({@link LayoutDefinition})を生成する。
     *
     * @param charset 各データのエンコーディング
     * @return レイアウト定義
     */
    private static LayoutDefinition createLayoutDefinition(Charset charset) {
        // フィールド定義
        FieldDefinition fd1 = new FieldDefinition()
                .setPosition(1)
                .setEncoding(charset)
                .addConvertorSetting("X", new Object[]{100})
                .setName("data1");
        ByteStreamDataString type = new ByteStreamDataString();
        type.init(fd1, new Object[]{100});
        fd1.setDataType(type);

        FieldDefinition fd2 = new FieldDefinition()
                .setPosition(101)
                .setEncoding(charset)
                .addConvertorSetting("XN", new Object[]{200})
                .setName("data2");
        type = new ByteStreamDataString();
        type.init(fd1, new Object[]{200});
        fd2.setDataType(type);

        FieldDefinition fd3 = new FieldDefinition()
                .setPosition(301)
                .setEncoding(charset)
                .addConvertorSetting("X", new Object[]{300})
                .setName("data3");
        type = new ByteStreamDataString();
        type.init(fd1, new Object[]{500});
        fd3.setDataType(type);

        // レコード定義
        RecordDefinition rd = new RecordDefinition()
                .setTypeName("test-record")
                .addField(fd1, fd2, fd3);

        // レイアウト定義
        LayoutDefinition ld = new LayoutDefinition();
        ld.addRecord(rd);
        ld.getDirective().put("file-type", "xml");
        ld.getDirective().put("record-length", 800);

        return ld;
    }
}

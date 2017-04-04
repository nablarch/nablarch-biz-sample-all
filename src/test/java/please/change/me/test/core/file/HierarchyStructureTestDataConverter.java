package please.change.me.test.core.file;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import nablarch.core.dataformat.DataRecord;
import nablarch.core.dataformat.FieldDefinition;
import nablarch.core.dataformat.LayoutDefinition;
import nablarch.core.dataformat.RecordDefinition;

import nablarch.test.core.file.TestDataConverter;

/**
 * 階層構造データ用のテストデータコンバータ。
 * <p/>
 * 本実装では、Excelファイルから読み込んだデータを元にレイアウト定義を生成する。
 *
 * 使用方法<br/>
 * <pre>
 * 1.Excelのメッセージングデータの期待値にfile-typeを設定する。
 * 例：
 *
 * +---------------+----------------+
 * | text-encoding | UTF-8          |
 * +---------------+----------------+
 * | file-type     | xml            |
 * +---------------+----------------+
 *
 * 2.本クラスを、No1で設定したfile-type用のTestDataConverterクラスとしてリポジトリに登録する。
 *
 * クラス登録時のcomponent nameは、「TestDataConverter_」 + "file-type"とする。
 * 本設定例の場合、file-type(No1で設定した値)が「xml」であるため、
 * component nameは「TestDataConverter_xml」となる。
 *
 * 例：
 * {@code
 * <component
 *     name="TestDataConverter_xml"
 *     class="please.change.me.test.core.file.HierarchyStructureTestDataConverter"/>
 * }
 * </pre>
 *
 * @author hisaaki sioiri
 */
public class HierarchyStructureTestDataConverter implements TestDataConverter {

    /**
     * {@inheritDoc}
     * <p/>
     * 本実装では、レイアウト定義のフィールドサイズ情報をカレントレコードの長さで上書きする。
     */
    @Override
    public LayoutDefinition createDefinition(LayoutDefinition defaultDefinition, DataRecord currentData,
            Charset encoding) {

        if (defaultDefinition == null) {
            throw new IllegalArgumentException("layout definition is null.");
        }

        if (currentData == null) {
            throw new IllegalArgumentException("current data is null.");
        }

        if (encoding == null) {
            throw new IllegalArgumentException("encoding is null");
        }

        RecordDefinition recordDefinition = defaultDefinition.getRecords().get(0);
        List<FieldDefinition> fields = recordDefinition.getFields();

        if (fields.size() != currentData.size()) {
            throw new IllegalArgumentException(
                    "count of field  of layout definition and current record are different."
                            + "layout definition: " + fields.size()
                            + ", current record: " + currentData.size()
            );
        }

        LayoutDefinition result = new LayoutDefinition();
        result.getDirective().putAll(defaultDefinition.getDirective());

        RecordDefinition newRecordDefinition = new RecordDefinition();
        newRecordDefinition.setTypeName(recordDefinition.getTypeName());

        int recordLength = 0;
        // 各フィールド定義のデータ長及びポジションをカレントデータを元に変更する
        for (FieldDefinition field : fields) {
            String data = currentData.getString(field.getName());
            if (data == null) {
                throw new IllegalArgumentException("field does not exist in the current record. field name: " + field.getName());
            }
            int length = data.getBytes(encoding).length;

            FieldDefinition newField = new FieldDefinition();
            newField.setName(field.getName());
            newField.setEncoding(field.getEncoding());
            newField.setPosition(recordLength + 1);
            for (Map.Entry<String, Object[]> entry : field.getConvertorSettingList().entrySet()) {
                // Convertor設定は、データタイプのみであることを前提として、
                // Convertor設定の引数を今回のデータ長に置き換える
                newField.addConvertorSetting(entry.getKey(), new Object[] {length});
            }
            newRecordDefinition.addField(newField);
            recordLength += length;
        }

        result.addRecord(newRecordDefinition);
        result.getDirective().put("record-length", recordLength);
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * 本実装では、引数で指定された現在処理中のデータをそのまま返却する。
     */
    @Override
    public DataRecord convertData(LayoutDefinition definition, DataRecord currentData, Charset encoding) {
        return currentData;
    }
}


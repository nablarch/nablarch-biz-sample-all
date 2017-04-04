package please.change.me.test.core.file;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

import nablarch.core.dataformat.DataRecord;
import nablarch.core.dataformat.FieldDefinition;
import nablarch.core.dataformat.LayoutDefinition;
import nablarch.core.dataformat.RecordDefinition;
import nablarch.test.core.file.TestDataConverter;

/**
 * キー=バリュー形式データテスト用のテストデータコンバータ<br>
 * 
 * 本実装ではエクセルファイルより読み込んだテストデータに対し、URLエンコーディングを行います。<br>
 * また、単一のダミーカラムに全値を設定し、テストデータの比較を行うよう、テスト用レイアウト定義の作成を行います。
 * 
 * @author TIS
 */
public class FormUrlEncodedTestDataConverter implements TestDataConverter {
    
    /** ダミーカラム名 */
    private static final String DUMMY_COL_NAME = "DUMMY";
    
    /**
     * {@inheritDoc}<br>
     * この実装ではテストデータの全カラムが結合されたダミーデータを取得し、その単一データを読み取るレイアウト定義を生成します。
     */
    public LayoutDefinition createDefinition(LayoutDefinition defaultDefinition, DataRecord currentData, Charset encoding) {
        if (currentData == null) {
            throw new IllegalArgumentException("data is null");
        }
        if (!currentData.containsKey(DUMMY_COL_NAME)) {
            throw new IllegalArgumentException("data is not contains dummy column. key=[" + DUMMY_COL_NAME + "]");
        }
        
        int recordLength = currentData.get(DUMMY_COL_NAME).toString().length();
        
        // フィールド定義
        FieldDefinition fd = new FieldDefinition()
                            .setPosition(1)
                            .setName(DUMMY_COL_NAME)
                            .setEncoding(encoding)
                            .addConvertorSetting("X", new Object[]{recordLength});

        // レコード定義
        RecordDefinition rd = new RecordDefinition()
                              .setTypeName(DUMMY_COL_NAME)
                              .addField(fd);
        
        // レイアウト定義
        LayoutDefinition ld = new LayoutDefinition();
        ld.addRecord(rd);
        ld.getDirective().putAll(defaultDefinition.getDirective());
        ld.getDirective().put("file-type", "Fixed");
        ld.getDirective().put("record-length", recordLength);
        return ld;
    }


    /**
     * {@inheritDoc}<br>
     * この実装ではテストデータの全カラムを結合します。<br>
     * また、各セルに[key=value]形式でデータが格納されていることを前提とし、
     * "="以降のvalue部に対し、URLエンコーディングを行います。
     */
    @Override
    public DataRecord convertData(LayoutDefinition definition, DataRecord currentData, Charset encoding) {
        StringBuilder sb = new StringBuilder();
        List<FieldDefinition> fdList = definition.getRecords().get(0).getFields();
        for (int i=0; i<fdList.size(); i++) {
            String key = fdList.get(i).getName();
            String value = currentData.getString(key);
            int sepIdx = (value == null)
                       ? -1
                       : value.indexOf("=");
            
            if (sepIdx < 0) {
                throw new IllegalArgumentException("record contains not 'key=value' format data. col=" + key);
                
            } else {
                String fueKey = value.substring(0, sepIdx).trim();
                String fueVal = value.substring(sepIdx + 1).trim();
                try {
                    fueVal = URLEncoder.encode(fueVal, encoding.name());
                } catch (UnsupportedEncodingException e) {
                    // NOP(発生し得ない例外)
                }
                if (i == (fdList.size() - 1)) {
                    sb.append(fueKey + "=" + fueVal);
                } else {
                    sb.append(fueKey + "=" + fueVal + "&");
                }
            }
        }
        DataRecord newData = new DataRecord();
        newData.put(DUMMY_COL_NAME, sb.toString());
        return newData;
    }
}

package please.change.me.core.dataformat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import nablarch.core.dataformat.DataRecord;
import nablarch.core.dataformat.DataRecordFormatter;
import nablarch.core.dataformat.DataRecordFormatterSupport;
import nablarch.core.dataformat.FieldDefinition;
import nablarch.core.dataformat.InvalidDataFormatException;
import nablarch.core.dataformat.RecordDefinition;
import nablarch.core.dataformat.convertor.datatype.CharacterStreamDataString;
import nablarch.core.dataformat.convertor.value.ValueConvertor;
import nablarch.core.util.FileUtil;
import nablarch.core.util.annotation.Published;
import please.change.me.core.dataformat.convertor.FormUrlEncodedDataConvertorSetting;

/**
 * フォーマット定義ファイルの内容に従い、キー=バリュー形式データの読み書きを行うクラス。
 * <p>
 * 本クラスはスレッドセーフを考慮した実装にはなっていないので、呼び出し元で同期化の制御を行うこと。
 * </p>
 * <p>
 * 本クラスはapplication/x-www-form-urlencoded形式に基づきデータの読み書きを行う。<br/>
 * </p>
 * <b>ディレクティブの設定</b>
 * <p>
 * キー=バリュー形式データを読み込む際は、以下のディレクティブの設定が必須となる。
 * <ul>
 * <li>ファイルの文字エンコーディング</li>
 * </ul>
 * </p>
 * @author TIS
 */
public class FormUrlEncodedDataRecordFormatter extends DataRecordFormatterSupport {

    /** フィールド分割文字列 */
    private static final Character FIELD_SEPARATOR = '&';
    
    /** キー値分割文字列 */
    private static final Character KEY_VALUE_SEPARATOR = '=';
    
    /** データ走査時のデリミタパターン */
    private static final Pattern SCANNER_DELIMITER = Pattern.compile("[" + FIELD_SEPARATOR + KEY_VALUE_SEPARATOR + "]");

    /** コンバータの設定情報保持クラス */
    private FormUrlEncodedDataConvertorSetting convertorSetting;

    /** 入力ストリーム。 */
    private InputStream source;
    
    /** ファイル読み込みに使用するリーダ */
    private Reader reader;
    
    /** 出力ストリーム。 */
    private OutputStream dest; 
    
    /** ファイル書き込みに使用するライタ */
    private Writer writer;

    @Override
    public String getMimeType() {
        return "application/x-www-form-urlencoded";
    }

    /**
     * デフォルトコンストラクタ。
     * デフォルトでは、VariableLengthConvertorSettingをコンバータとして使用する。
     */
    @Published(tag = "architect")
    public FormUrlEncodedDataRecordFormatter() {
        convertorSetting = FormUrlEncodedDataConvertorSetting.getInstance();
    }

    /** {@inheritDoc} 
     * <p/>
     * また、入力ストリームをBufferedReaderにラップする処理および、
     * 出力ストリームをBufferedWriterにラップする処理を行う。
     */
    public DataRecordFormatter initialize() {        
        super.initialize();
        if (source != null && reader == null) {  // reader生成済みの場合は初期化しない
            initializeReader();
        } 
        if (dest != null) {
            initializeWriter();
        }
        return this;
    }

    /** {@inheritDoc} */
    public DataRecordFormatter setInputStream(InputStream stream) {
        source = stream;
        return this;
    }

    /** {@inheritDoc} */
    public DataRecordFormatter setOutputStream(OutputStream stream) {
        dest = stream;
        return this;
    }

    /**
     * フォーマット定義ファイルで指定されたエンコーディングで、可変長データを読み込むリーダを生成する。
     */
    protected void initializeReader() {
        this.reader = new BufferedReader(new InputStreamReader(source,
                getDefaultEncoding()));
    }

    /**
     * ライタを生成する。
     */
    protected void initializeWriter() {
        this.writer = new BufferedWriter(
            new OutputStreamWriter(dest, getDefaultEncoding())
        );
    }

    /**
     * ストリームから全てのパラメータを読み取ります。
     * @return 全てのパラメータを格納したマップ
     * @throws IOException 読み込みに伴うIO処理で問題が発生した場合
     */
    private Map<String, List<String>> readAllParameters() throws IOException {
        Scanner sc = new Scanner(reader);
        sc.useDelimiter(SCANNER_DELIMITER);

        Charset encoding = getDefaultEncoding();
        
        // いったん全てのパラメータを読み込む
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        while (sc.hasNext()) {
            String key = sc.next();
            String value;
            if (sc.hasNext()) {
                value = sc.next();
            } else {
                value = ""; 
            }
            
            // URLデコード
            value = URLDecoder.decode(value, encoding.name());
            
            // 同一キーで複数個存在する可能性があるのでリストにする
            List<String> list = map.get(key);
            if (list == null) {
                list = new ArrayList<String>();
                map.put(key, list);
            }
            list.add(value);
        }

        return map;
    }
    
    /**{@inheritDoc} */
    @Published(tag = "architect")
    public DataRecord readRecord() throws IOException {
        
        if (reader == null) {
            throw new IllegalStateException("input stream was not set. input stream must be set before reading.");
        }
        
        if (!hasNext()) {
            return null;
        }

        incrementRecordNumber(); // レコード番号をインクリメントする

        Map<String, List<String>> map = readAllParameters();
        
        // フィールドを精査、レコードに格納する
        DataRecord record = new DataRecord();
        RecordDefinition recordDef = getDefinition().getRecords().get(0); // シングルレコードフォーマットのみ
        for (FieldDefinition fieldDef : recordDef.getFields()) {
            String fieldName = fieldDef.getName();
            List<String> list = map.get(fieldName); // 非配列の項目もいったんリストになっている
            if (list == null && fieldDef.isRequired()) {
                // 必須項目が存在しない
                throw new InvalidDataFormatException(fieldName + " is required");
            }
            
            // まず値を変換
            List<Object> convertedList = new ArrayList<Object>();
            if (list == null) {
                convertedList.add(convertToField(null, fieldDef));
            } else {
                for (String str : list) {
                    convertedList.add(convertToField(str, fieldDef));
                }
            }
            
            if (fieldDef.isArray()) {
                // 配列範囲チェック
                if (fieldDef.getMinArraySize() > convertedList.size() || fieldDef.getMaxArraySize() < convertedList.size()) {
                    throw new InvalidDataFormatException(fieldName + " is out of range array");
                }
                
                // DataRecordの配列はString配列のみのため、変換する
                String[] array = new String[convertedList.size()];
                for (int i = 0; i < convertedList.size(); i++) {
                    Object obj = convertedList.get(i);
                    array[i] = (obj == null)
                             ? null
                             : obj.toString();
                }
                
                record.put(fieldName, array);
                
            } else {
                // 配列以外でデータが複数の場合のチェック
                if (convertedList.size() > 1) {
                    throw new InvalidDataFormatException(fieldName + " is not array but many keys");
                }
                record.put(fieldName, convertedList.get(0));
            }
        }

        return record;
    }

    /**
     * 読み込んだフィールド文字列をコンバータを用いてオブジェクトに変換し、返却する。
     * @param fieldStr 読み込んだフィールド文字列
     * @param field フィールド定義情報保持クラス
     * @return コンバートしたフィールドの内容
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Object convertToField(String fieldStr, FieldDefinition field) {
        
        CharacterStreamDataString dataType = 
                (CharacterStreamDataString) field.getDataType();

        Object value;
        try {
            // データタイプのコンバータを実行する
            value = dataType.convertOnRead(fieldStr);
    
            // コンバータを実行する
            for (ValueConvertor convertor : field.getConvertors()) { 
                value = convertor.convertOnRead(value);
            }
        } catch (InvalidDataFormatException e) {
            // コンバータで発生した例外に対して、フィールド名の情報を付与する
            throw e.setFieldName(field.getName());
        }
        return value;
    }


    /** {@inheritDoc} */
    public void writeRecord(Map<String, ?> record)
        throws IOException, InvalidDataFormatException  {
        writeRecord(null, record);
    }

    /** {@inheritDoc} */
    public void writeRecord(String recordType, Map<String, ?> record)
        throws IOException {
        
        if (writer == null) {
            throw new IllegalStateException(
                    "output stream was not set. output stream must be set before writing.");
        }
        
        incrementRecordNumber(); // レコード番号をインクリメントする

        RecordDefinition writeRecordDef = getDefinition().getRecords().get(0); // シングルレコードフォーマットのみ
        
        writeRecord(record, writeRecordDef);
    }

    /**
     * 1レコード分の内容を、出力ストリームへ書き込む。
     * @param record 出力するレコードの内容を格納したMap
     * @param recordType レコードタイプ
     * @throws IOException 書き込みに伴うIO処理で問題が発生した場合。
     */
    private void writeRecord(Map<String, ?> record, RecordDefinition recordType)
        throws IOException {
        
        boolean isWroteBefore = false;
        for (int i = 0; i < recordType.getFields().size(); i++) {
            isWroteBefore = writeField(record, recordType.getFields().get(i), isWroteBefore);
        }
        writer.flush();
    }

    /**
     * コンバータによる変換を行ったフィールドの内容を、出力ストリームへ書き込む。
     * @param record 出力するレコードの内容を格納したMap
     * @param field  フィールド定義情報保持クラス
     * @param isWroteBefore 前の値が書き込まれたかどうか
     * @return 書き込んだかどうか
     * @throws IOException 書き込みに伴うIO処理で問題が発生した場合
     */
    private boolean writeField(Map<String, ?> record, FieldDefinition field, boolean isWroteBefore)
        throws IOException {

        String fieldName = field.getName();
        
        // 配列を分解し、リスト化
        Object data = record.get(fieldName);
        List<Object> list = new ArrayList<Object>();
        if (data != null && data instanceof String[]) {
            for (String str : (String[]) data) {
                list.add(str);
            }
        } else {
            list.add(data);
        }
        
        if (!field.isArray() && list.size() > 1) {
            // 配列以外でデータが複数の場合のチェック
            throw new InvalidDataFormatException(fieldName + " is not array but many keys");
        }
        
        if (field.isArray() && (field.getMinArraySize() > list.size() || field.getMaxArraySize() < list.size())) {
            // 配列範囲チェック
            throw new InvalidDataFormatException(fieldName + " is out of range array");
        }
        
        // 値の書き込み
        return writeField(list, field, isWroteBefore);
    }
    
    /**
     * コンバータによる変換を行ったフィールドの内容を、出力ストリームへ書き込む。
     * @param list 出力するレコードの内容を格納したList
     * @param field フィールド定義情報保持クラス
     * @param isWroteBefore 前の値が書き込まれたかどうか
     * @return 書き込んだかどうか
     * @throws IOException 書き込みに伴うIO処理で問題が発生した場合
     */
    @SuppressWarnings("rawtypes")
    private boolean writeField(List<Object> list, FieldDefinition field, boolean isWroteBefore)
        throws IOException {
        
        Charset encoding = getDefaultEncoding();
        String fieldName = field.getName();
        
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            
            String outData;
            try {
                // コンバータを実行する
                for (ValueConvertor convertor : field.getConvertors()) {
                    obj = convertor.convertOnWrite(obj);
                }
                
                // データタイプを実行する       
                CharacterStreamDataString dataType = 
                        (CharacterStreamDataString) field.getDataType();
                
                outData = dataType.convertOnWrite(obj);
                
            } catch (InvalidDataFormatException e) {
                // コンバータで発生した例外に対してフィールド名の情報を付与する
                throw e.setFieldName(fieldName);
            }
            
            if (obj == null) {
                if (field.isRequired()) {
                    // 必須項目がnull
                    throw new InvalidDataFormatException(fieldName + " is required");
                } else {
                    continue;
                }
                
            } else {
                if (isWroteBefore) {
                    writer.write(FIELD_SEPARATOR);
                }
                
                writer.write(fieldName);
                writer.write(KEY_VALUE_SEPARATOR);
                writer.write(URLEncoder.encode(outData, encoding.name()));
                
                isWroteBefore = true;
            }
        }
        return isWroteBefore;
    }

    
    /** {@inheritDoc}
     */
    public boolean hasNext() throws IOException {
        if (source == null) {
            return false;
        }
        source.mark(1);
        int readByte = source.read();
        source.reset();
        return (readByte != -1);
    }
    
    /**
     * {@inheritDoc}
     * この実装では、{@link #setInputStream}メソッドおよび{@link #setOutputStream}メソッドで渡されたストリーム、
     * および内部でそれらをラップする{@link Reader}、{@link Writer}のストリームをクローズする。
     */
    public void close() {
        FileUtil.closeQuietly(reader, source, writer, dest);
    }

    /**
     *  コンバータの設定情報保持クラスを取得する
     *  @return コンバータの設定情報保持クラス
     */
    protected FormUrlEncodedDataConvertorSetting getConvertorSetting() {
        return convertorSetting;
    }
}

package please.change.me.simulator.common;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * 各種データとMapの相互変換結果を格納するクラス。
 * 
 * @author TIS
 * @since 1.4
 */
class SimpleDataConvertResult {

    /** データ種別 */
    private String dataType = null;
    /** mime-type */
    private String mimeType = "text/plain";
    /** 文字セット */
    private Charset charset = Charset.defaultCharset();
    /** 結果文字列 */
    private String resultText = null;
    /** 結果Map */
    private Map<String, ?> resultMap = null;
    
    /**
     * コンストラクタ
     */
    SimpleDataConvertResult() {
        super();
    }
    
    /**
     * データ種別を返却する。
     * @return データ種別
     */
    public String getDataType() {
        return dataType;
    }
    
    /**
     * データ種別を設定する。
     * @param dataType データ種別
     * @return このオブジェクト自体
     */
    SimpleDataConvertResult setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }
    
    /**
     * mime-typeを返却する。
     * @return mime-type
     */
    public String getMimeType() {
        return mimeType;
    }
    
    /**
     * mime-typeを設定する。
     * @param mimeType mime-type
     * @return このオブジェクト自体
     */
    SimpleDataConvertResult setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }
    
    /**
     * 文字セットを返却する。
     * @return 文字セット
     */
    public Charset getCharset() {
        return charset;
    }
    
    /**
     * 文字セットを設定する。
     * @param charset 文字セット
     * @return このオブジェクト自体
     */
    SimpleDataConvertResult setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }
    
    /**
     * 結果テキストを返却する。
     * @return 結果テキスト
     */
    public String getResultText() {
        return resultText;
    }
    
    /**
     * 結果テキストを設定する。
     * @param resultText 結果テキスト
     * @return このオブジェクト自体
     */
    SimpleDataConvertResult setResultText(String resultText) {
        this.resultText = resultText;
        return this;
    }
    
    /**
     * 結果Mapを返却する。
     * @return 結果Map
     */
    public Map<String, ?> getResultMap() {
        return resultMap;
    }
    
    /**
     * 結果Mapを設定する。
     * @param resultMap 結果Map
     * @return このオブジェクト自体
     */
    SimpleDataConvertResult setResultMap(Map<String, ?> resultMap) {
        this.resultMap = resultMap;
        return this;
    }
}

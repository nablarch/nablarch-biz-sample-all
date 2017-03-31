package please.change.me.fw.web.useragent;


/**
 * UserAgent判定・項目値変換インタフェース。
 * 
 * @author TIS
 */
public interface UserAgentValueConvertor {

    /**
     * 項目値の変換を行う。
     * 
     * @param value 変換対象の値
     * @return 変換後の値
     */
    String convert(String value);

}

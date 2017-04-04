package please.change.me.fw.web.useragent;


/**
 * UserAgent判定・項目名称変換デフォルト実装クラス。
 * 
 * <p>
 * 本クラスでは指定された置換前文字列を置換後文字列に単純に置き換える。
 * また、toLowerCaseプロパティが設定されている場合は置換前に文字列を小文字化する。
 * <br>
 * 例）「Mac os x」→「mac_os_x」
 * </p>
 * 
 * @author TIS
 */
public class UserAgentNameConvertor implements UserAgentValueConvertor {

    /** 小文字化指定 */
    private boolean isToLowerCase = true;
    
    /** 置換前文字列 */
    private String replaceFrom;

    /** 置換後文字列 */
    private String replaceTo;
    
    /**
     * {@inheritDoc}
     */
    public String convert(String value) {
        boolean shouldReplace = checkReplaceSetting();
        if (value == null) {
            return null;
        }
        
        if (isToLowerCase) {
            value = value.toLowerCase();
        }

        if (shouldReplace) {
            value = value.replace(replaceFrom, replaceTo);
        }
        return value;
    }

    /**
     * 置換設定のチェックを行う。
     * 以下の場合、正常とみなす。
     * <ul>
     * <li>{@link #replaceFrom}と{@link #replaceTo}が両方共設定済み</li>
     * <li>{@link #replaceFrom}と{@link #replaceTo}が両方共未設定</li>
     * </ul>
     *
     * {@link #replaceFrom}と{@link #replaceTo}が両方共設定済みの場合、真を返却する。
     * @return 置換を行う必要がある場合、真
     */
    private boolean checkReplaceSetting() {
        if (replaceFrom == null ^ replaceTo == null) {
            throw new IllegalStateException("replaceFrom and replaceTo must be set both.");
        }
        return replaceFrom != null;    // 片方nullでなければ、もう片方もnullでない。
    }

    /**
     * 小文字化指定を設定する。
     * @param isToLowerCase 小文字化指定
     */
    public void setToLowerCase(boolean isToLowerCase) {
        this.isToLowerCase = isToLowerCase;
    }
    
    /**
     * 置換前文字列を設定する。
     * @param replaceFrom 置換前文字列
     */
    public void setReplaceFrom(String replaceFrom) {
        this.replaceFrom = replaceFrom;
    }

    /**
     * 置換後文字列を設定する。
     * @param replaceTo 置換後文字列
     */
    public void setReplaceTo(String replaceTo) {
        this.replaceTo = replaceTo;
    }

}

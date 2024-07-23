package please.change.me.statistics.exception;

/**
 * ログ解析に失敗した事を示す例外
 *
 * @author hisaaki sioiri
 */
public class LogParseException extends RuntimeException {

    /**
     * コンストラクタ。
     *
     * @param message 例外の内容を示すメッセージ
     */
    public LogParseException(String message) {
        super(message);
    }

    /**
     * コンストラクタ。
     *
     * @param message 例外の内容を示すメッセージ
     * @param cause 元例外
     */
    public LogParseException(String message, Throwable cause) {
        super(message, cause);
    }
}

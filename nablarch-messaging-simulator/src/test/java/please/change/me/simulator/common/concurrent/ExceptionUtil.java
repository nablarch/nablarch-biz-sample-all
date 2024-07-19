package please.change.me.simulator.common.concurrent;

/**
 * 例外を扱うユーティリティクラス。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class ExceptionUtil {

    /**
     * 与えられた{@link Throwable}を非チェック例外に変換する。
     * 引数が、もともと非チェック例外またはエラーである場合は、
     * その引数がそのままスローされる。
     *
     * @param orig Throwable
     * @return 非チェック例外
     */
    public static RuntimeException convertToRuntime(Throwable orig) {
        if (orig instanceof RuntimeException) {
            throw (RuntimeException) orig;
        }
        if (orig instanceof Error) {
            throw (Error) orig;
        }
        return new RuntimeException(orig);
    }
}

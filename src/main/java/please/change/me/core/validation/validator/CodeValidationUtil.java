package please.change.me.core.validation.validator;

import java.util.HashMap;
import java.util.Map;

import nablarch.common.code.validator.CodeValue;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationUtil;

/**
 * コード値精査を行うためのユーティリティクラス。
 *
 * @author hisaaki sioiri
 */
public final class CodeValidationUtil {

    /** 隠蔽コンストラクタ */
    private CodeValidationUtil() {
    }

    /**
     * コード値精査を行う。<br/>
     *
     * 指定されたプロパティの値が、指定されたコードIDとパターンで有効か否かを精査する。
     * 精査エラー時には、指定された{@link nablarch.core.validation.ValidationContext}にエラーメッセージを格納する。
     *
     * 既に対象のプロパティーで精査エラーが発生している場合には、精査は行わない。
     *
     * @param context 精査コンテキスト
     * @param codeId コードID
     * @param pattern パターン
     * @param propertyName 精査対象のプロパティ
     */
    public static void validate(ValidationContext<?> context, String codeId, String pattern, String propertyName) {
        validate(context, codeId, pattern, propertyName, null);
    }

    /**
     * コード値精査を行う。<br/>
     *
     * 指定されたプロパティの値が、指定されたコードIDとパターンで有効か否かを精査する。
     * 精査エラー時には、指定された{@link nablarch.core.validation.ValidationContext}に
     * メッセージIDに紐づくエラーメッセージを格納する。
     *
     * 既に対象のプロパティーで精査エラーが発生している場合には、精査は行わない。
     *
     * @param context 精査コンテキスト
     * @param codeId コードID
     * @param pattern パターン
     * @param propertyName 精査対象のプロパティ
     * @param messageId メッセージID
     */
    public static void validate(ValidationContext<?> context, String codeId, String pattern, String propertyName, String messageId) {
        if (context.isInvalid(propertyName)) {
            return;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("codeId", codeId);
        params.put("pattern", pattern);
        params.put("messageId", messageId);
        ValidationUtil.validate(context, propertyName, CodeValue.class, params);
    }
}

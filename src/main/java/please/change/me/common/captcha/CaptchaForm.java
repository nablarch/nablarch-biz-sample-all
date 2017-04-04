package please.change.me.common.captcha;

import java.util.Map;

import nablarch.core.validation.PropertyName;
import nablarch.core.validation.ValidateFor;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationUtil;
import nablarch.core.validation.validator.Length;
import nablarch.core.validation.validator.Required;
import nablarch.fw.web.HttpRequest;

/**
 * CAPTCHA認証用フォーム。
 *
 * @author TIS
 */
public class CaptchaForm {

    /** 識別キー */
    private String captchaKey;

    /**
     * コンストラクタ。
     *
     * @param data インプットデータ
     */
    public CaptchaForm(Map<String, Object> data) {
        captchaKey = (String) data.get("captchaKey");
    }
    

    /**
     * 識別キーを取得する。
     *
     * @return 識別キー
     */
    public String getCaptchaKey() {
        return captchaKey;
    }

    /**
     * 識別キーを設定する。
     *
     * @param captchaKey 識別キー
     */
    @PropertyName("識別キー")
    @Required
    @Length(max = 40)
    public void setCaptchaKey(String captchaKey) {
        this.captchaKey = captchaKey;
    }

    /**
     * バリデーションを実施する。
     *
     * @param req 入力パラメータ情報
     * @param validationName 使用するバリデーションの名前
     * @return 入力パラメータを精査後に生成した本フォーム
     */
    public static CaptchaForm validate(HttpRequest req, String validationName) {
        ValidationContext<CaptchaForm> context = ValidationUtil.validateAndConvertRequest(
                CaptchaForm.class, req, validationName);
        context.abortIfInvalid();
        return context.createObject();
    }
    
    /**
     * CAPTCHA認証用の精査を行う。
     *
     * @param context バリデーションの実行に必要なコンテキスト
     */
    @ValidateFor("captcha")
    public static void validateForCaptcha(ValidationContext<CaptchaForm> context) {
        ValidationUtil.validate(context, new String[]{"captchaKey"});
    }

}

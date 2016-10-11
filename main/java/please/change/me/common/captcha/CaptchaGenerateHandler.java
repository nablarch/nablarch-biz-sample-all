package please.change.me.common.captcha;

import nablarch.core.message.ApplicationException;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpRequestHandler;
import nablarch.fw.web.HttpResponse;

/**
 * CAPTCHA情報生成アクションクラス。
 * 
 * @author TIS
 */
public class CaptchaGenerateHandler implements HttpRequestHandler {
    
    /**
     * {@inheritDoc}<br>
     * 
     * この実装ではCAPTCHA画像の生成を行う。
     */
    @Override
    public HttpResponse handle(HttpRequest request, ExecutionContext context) {
        
        Captcha captcha = null;
        try {
            // 入力値の精査
            String keyParam = CaptchaForm.validate(request, "captcha").getCaptchaKey();

            // CAPTCHA情報生成
            captcha = CaptchaUtil.generateImage(keyParam);
            
        } catch (ApplicationException e) {
            throw createEmptyErrorResponse(e, 400);
        } catch (RuntimeException e) {
            throw createEmptyErrorResponse(e, 500);
        }
        
        if (captcha ==  null) {
            throw createEmptyErrorResponse(new ApplicationException(), 400);
        }
        return new HttpResponse().write(captcha.getImage());

    }
    
    /**
     * エラーレスポンスの設定（ボディ部が空のエラーレスポンスを返却）
     * @param e 実行時例外
     * @param statusCd ステータスコード
     * @return ボディ部が空のエラーレスポンス
     */
    private HttpErrorResponse createEmptyErrorResponse(Throwable e, int statusCd) {
        // ボディ部が空のエラーレスポンス
        return new HttpErrorResponse(e).setResponse(new HttpResponse(statusCd).write(""));
    }
}

package please.change.me.common.captcha;

/**
 * エラーCAPTCHA情報ジェネレータ
 * @author TIS
 */
public class ErrorGenerator extends CaptchaGenerator {

    @Override
    public String createText() {
        throw new RuntimeException("DUMMY_ERROR_MESSAGE");
    }

    @Override
    public byte[] createImage(String text) {
        throw new RuntimeException("DUMMY_ERROR_MESSAGE");
    }
}
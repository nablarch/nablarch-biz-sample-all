package please.change.me.common.captcha;

/**
 * ダミーCAPTCHA情報ジェネレータ
 * @author TIS
 */
public class DummyGenerator extends CaptchaGenerator {

    @Override
    public String createText() {
        return "DUMMY_TEXT";
    }

    @Override
    public byte[] createImage(String text) {
        return "DUMMY_IMAGE".getBytes();
    }
}
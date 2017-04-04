package please.change.me.common.captcha;

import java.util.UUID;

import nablarch.core.date.SystemTimeUtil;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.StringUtil;

/**
 * キャプチャ認証機能ユーティリティ。
 * 
 * @author TIS
 */
public final class CaptchaUtil {
    
    /** CAPTCHAデータマネージャ */
    private static CaptchaDataManager captchaDataManager = new CaptchaDataManager();
    
    /**
     * 隠蔽コンストラクタ
     */
    private CaptchaUtil() {
        // nop
    }
    
    /**
     * CAPTCHA情報の識別キーを生成する。
     * 
     * @return CAPTCHA情報の識別キー
     */
    public static String generateKey() {
        String key = UUID.randomUUID().toString();
        
        captchaDataManager.create(key);
        
        return key;
    }
    
    /**
     * CAPTCHA情報を生成します。
     * 
     * @param key CAPTCHA情報の識別キー
     * @return CAPTCHA情報。生成に失敗した場合はnull
     */
    static Captcha generateImage(String key) {
        
        CaptchaGenerator captchaGenerator = SystemRepository.get("captchaGenerator");
        if (captchaGenerator == null) {
            captchaGenerator = new CaptchaGenerator();
        }
        
        String text = captchaGenerator.createText();
        byte[] image = captchaGenerator.createImage(text);
        
        Captcha captcha = new Captcha()
                            .setText(text)
                            .setImage(image)
                            .setKey(key)
                            .setGenerateDateTime(SystemTimeUtil.getTimestamp());
        
        return captchaDataManager.save(captcha) ? captcha : null;
    }
    
    /**
     * CAPTCHA情報の認証を行います。
     * 
     * @param key 識別キー
     * @param inputValue 入力値
     * @return 入力値が正しい場合true
     */
    public static boolean authenticate(String key, String inputValue) {
        if (StringUtil.isNullOrEmpty(key) || StringUtil.isNullOrEmpty(inputValue)) {
            return false;
        }
        
        Captcha captcha = captchaDataManager.load(key);
        
        if (captcha == null) {
            return false;
        }
        
        return inputValue.equals(captcha.getText());
    }
}

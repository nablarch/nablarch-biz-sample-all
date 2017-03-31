package please.change.me.common.captcha;

import java.util.Date;

/**
 * CAPTCHA情報を保持するクラス。
 * 
 * @author TIS
 */
public class Captcha {

    /** CAPTCHA文字列 */
    private String text;
    
    /** CAPTCHA画像 */
    private byte[] image;
    
    /** CAPTCHA情報管理キー */
    private String key;
    
    /** CAPTCHA情報生成日時 */
    private Date generateDateTime;
    
    /**
     * CAPTCHA文字列を取得する。
     * 
     * @return CAPTCHA文字列
     */
    public String getText() {
        return text;
    }
    
    /**
     * CAPTCHA文字列をセットする。
     * 
     * @param text CAPTCHA文字列
     * @return このオブジェクト自体
     */
    public Captcha setText(String text) {
        this.text = text;
        return this;
    }
    
    /**
     * CAPTCHA画像を取得する。
     * 
     * @return CAPTCHA画像
     */
    public byte[] getImage() {
        return image;
    }
    
    /**
     * CAPTCHA画像をセットする。
     * 
     * @param image CAPTCHA画像
     * @return このオブジェクト自体
     */
    public Captcha setImage(byte[] image) {
        this.image = image;
        return this;
    }
    
    /**
     * CAPTCHA情報管理キーを取得する。
     * 
     * @return CAPTCHA情報管理キー
     */
    public String getKey() {
        return key;
    }
    
    /**
     * CAPTCHA情報管理キーをセットする。
     * 
     * @param key CAPTCHA情報管理キー
     * @return このオブジェクト自体
     */
    public Captcha setKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * CAPTCHA情報生成日時を取得する。
     * @return CAPTCHA情報生成日時
     */
    public Date getGenerateDateTime() {
        return generateDateTime;
    }

    /**
     * CAPTCHA情報生成日時をセットする。
     * @param generateDateTime CAPTCHA情報生成日時
     * @return このオブジェクト自体
     */
    public Captcha setGenerateDateTime(Date generateDateTime) {
        this.generateDateTime = generateDateTime;
        return this;
    }
}

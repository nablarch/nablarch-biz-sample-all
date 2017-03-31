package please.change.me.common.captcha;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import nablarch.core.util.FileUtil;
import nablarch.core.util.StringUtil;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.util.Config;
import com.google.code.kaptcha.util.ConfigException;

/**
 * kaptchaを使用してCAPTCHA情報の生成を行うクラス。
 * 
 * @author TIS
 */
public class CaptchaGenerator {
    
    /** デフォルトの画像種別 */
    private static final String DEFAULT_IMAGE_TYPE = "jpeg";
    
    /** 画像種別 */
    private String imageType = DEFAULT_IMAGE_TYPE;
    
    /** kaptchaの設定パラメータ */
    private Config kaptchaConfig;
    
    /**
     * コンストラクタ
     */
    public CaptchaGenerator() {
        // 空のコンフィグで初期化する
        initProducer(null);
    }
    
    /**
     * {@inheritDoc}<br>
     * 
     * この実装ではkaptchaにより生成されたランダムな文字列を返却する。
     */
    public String createText() {
        try {
            Producer kaptchaProducer = kaptchaConfig.getProducerImpl();
            return kaptchaProducer.createText();
            
        } catch (ConfigException e) {
            throw new RuntimeException("Failed to create text.", e);
        }
    }
    
    /**
     * {@inheritDoc}<br>
     * 
     * この実装ではkaptchaにより生成された画像データを返却する。
     */
    public byte[] createImage(String text) {
        if (StringUtil.isNullOrEmpty(text)) {
            throw new IllegalArgumentException("text is null.");
        }
        
        BufferedImage bi;
        try {
            Producer kaptchaProducer = kaptchaConfig.getProducerImpl();
            bi = kaptchaProducer.createImage(text);
            
        } catch (ConfigException e) {
            throw new RuntimeException("Failed to create image. imageType=[" + imageType + "] text=[" + text + "]", e);
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if (!ImageIO.write(bi, imageType, baos)) {
                throw new IOException("Invalid image type was specified.");
            }
            return baos.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to write image. imageType=[" + imageType + "] text=[" + text + "]", e);
        } finally {
            FileUtil.closeQuietly(baos);
        }
    }
    
    /**
     * 設定パラメータをセットする。
     * 
     * @param configParameters セットする設定パラメータ
     * @return このオブジェクト自体
     */
    public CaptchaGenerator setConfigParameters(Map<String, String> configParameters) {
        initProducer(configParameters);
        return this;
    }
    
    /**
     * 画像種別をセットする。
     * 
     * @param imageType セットする画像種別
     * @return このオブジェクト自体
     */
    public CaptchaGenerator setImageType(String imageType) {
        this.imageType = imageType;
        return this;
    }
    
    /**
     * 指定された設定パラメータを使用して、kaptchaのコンフィグを初期化する。
     * 
     * @param configParameters 設定パラメータ
     */
    private void initProducer(Map<String, String> configParameters) {
        // 設定パラメータの変換
        Properties props = new Properties();
        if (configParameters != null) {
            props.putAll(configParameters);
        }
        
        // コンフィグを初期化
        kaptchaConfig = new Config(props);
    }
}

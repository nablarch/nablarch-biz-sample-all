package please.change.me.common.captcha;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import org.junit.Test;

/**
 * {@link CaptchaGenerator}のテスト
 *
 * @author TIS
 */
public class CaptchaGeneratorTest {

    /**
     * デフォルト設定での画像生成
     *
     * @throws Exception 例外
     */
    @Test
    public void testDefaultSetting() throws Exception {
        CaptchaGenerator generator = new CaptchaGenerator();
        String text = generator.createText();
        assertText(text, 5, "abcde2345678gfynmnpwx");
        byte[] image = generator.createImage(text);
        assertImage(image, "jpeg", 200, 50);
    }

    /**
     * kaptcha設定を変更
     *
     * @throws Exception 例外
     */
    @Test
    public void testCustomKaptchaSetting() throws Exception {
        Map<String, String> configParameters = new HashMap<String, String>() {
            {
                put("kaptcha.textproducer.char.string", "abc"); // テキストに使用する文字列
                put("kaptcha.textproducer.char.length", "10"); // テキストの長さ
                put("kaptcha.image.width", "100"); // 画像の幅
                put("kaptcha.image.height", "25"); // 画像の高さ
            }
        };

        CaptchaGenerator generator = new CaptchaGenerator();
        generator.setConfigParameters(configParameters);

        String text = generator.createText();
        assertText(text, 10, "abc");
        byte[] image = generator.createImage(text);
        assertImage(image, "jpeg", 100, 25);
    }

    /**
     * 不正なkaptcha設定
     *
     * @throws Exception 例外
     */
    @Test
    public void testInvalidKaptchaSetting() throws Exception {
        Map<String, String> configParameters = new HashMap<String, String>() {
            {
                put("kaptcha.textproducer.char.length", "10a"); // テキストの長さ
                put("kaptcha.image.width", "100a"); // 画像の幅
                put("kaptcha.image.height", "25a"); // 画像の高さ
            }
        };

        CaptchaGenerator generator = new CaptchaGenerator();
        generator.setConfigParameters(configParameters);

        try {
            generator.createText();
            fail();
        } catch (Exception e) {
            assertThat(e, is(instanceOf(RuntimeException.class)));
            assertEquals("Failed to create text.", e.getMessage());
        }

        try {
            generator.createImage("hoge");
            fail();
        } catch (Exception e) {
            assertThat(e, is(instanceOf(RuntimeException.class)));
            assertEquals("Failed to create image. imageType=[jpeg] text=[hoge]", e.getMessage());
        }
    }

    /**
     * 画像形式を変更
     *
     * @throws Exception 例外
     */
    @Test
    public void testCustomImageType() throws Exception {
        for (String formatName : ImageIO.getWriterFormatNames()) {
            if (imageExceptedExtension.contains(formatName.toLowerCase())) {
                continue;
            }
            CaptchaGenerator generator = new CaptchaGenerator();
            generator.setImageType(formatName);

            String text = generator.createText();
            byte[] image = generator.createImage(text);
            assertImage(image, formatName, 200, 50);
        }
    }

    /**
     * 不正な画像形式
     *
     * @throws Exception 例外
     */
    @Test
    public void testInvalidImageType() throws Exception {
        String imageType = "hoge";

        CaptchaGenerator generator = new CaptchaGenerator();
        generator.setImageType(imageType);

        String text = generator.createText();

        try {
            generator.createImage(text);
            fail();
        } catch (Exception e) {
            assertThat(e, is(instanceOf(RuntimeException.class)));
            assertEquals("Failed to write image. imageType=[" + imageType + "] text=[" + text + "]", e.getMessage());
        }
    }

    /**
     * 不正なテキスト
     *
     * @throws Exception 例外
     */
    @Test
    public void testInvalidText() throws Exception {
        CaptchaGenerator generator = new CaptchaGenerator();

        try {
            generator.createImage(null);
            fail();
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IllegalArgumentException.class)));
            assertEquals("text is null.", e.getMessage());
        }

        try {
            generator.createImage("");
            fail();
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IllegalArgumentException.class)));
            assertEquals("text is null.", e.getMessage());
        }
    }

    /**
     * 生成テキストの検証として、長さ、許可文字列を検証する
     * <p>
     * ただし、生成テキストはランダムなので、許可文字列の検証は不完全。
     * 指定文字列のみ使用して生成することはkaptchaの責務であるため、ここでは簡易的な検証にとどめる。
     *
     * @param actualText 生成テキスト
     * @param expectedLength 長さ
     * @param allowChars 許可文字列
     */
    private void assertText(String actualText, int expectedLength, String allowChars) {
        assertEquals(actualText, expectedLength, actualText.length());
        for (char c : actualText.toCharArray()) {
            assertTrue(actualText, allowChars.indexOf(c) >= 0);
        }
    }

    /**
     * 生成画像の検証として、形式、高さ、幅を検証する。
     * <p>
     * 画像に含まれるテキストの検証は行わない。行えない。プログラム的に簡単に解析できたら意味がない。
     *
     * @param actualImage 生成画像
     * @param expectedFormat 形式
     * @param expectedWidth 幅
     * @param expectedHeight 高さ
     * @throws IOException 例外
     */
    private void assertImage(byte[] actualImage, String expectedFormat, int expectedWidth, int expectedHeight) throws
            IOException {
        // 画像データを取得
        ImageInputStream imageIn = ImageIO.createImageInputStream(new ByteArrayInputStream(actualImage));
        Iterator<ImageReader> readers = ImageIO.getImageReaders(imageIn);

        ImageReader reader = null;
        if (readers.hasNext()) {
            reader = readers.next();
            reader.setInput(imageIn);
        }
        assertNotNull(reader);

        // 画像形式を検証
        String formatName = reader.getFormatName()
                                  .toLowerCase();

        final List<Matcher<String>> formatNameMatcherList = new ArrayList<Matcher<String>>();
        for (final String s : reader.getOriginatingProvider()
                                    .getFormatNames()) {
            formatNameMatcherList.add(is(s));
        }
        
        assertThat(reader.getOriginatingProvider().getFormatNames(), Matchers.hasItemInArray(formatName));
        // 画像サイズを検証
        assertEquals(expectedHeight, reader.getHeight(0));
        assertEquals(expectedWidth, reader.getWidth(0));
    }

    /**
     * イメージ生成時の除外拡張子。
     * {@link ImageIO#write} では {@link ImageIO#getWriterFormatNames()} で取得できるフォーマット名を使用できるはずだが、
     * 一部使用できないフォーマット名があるため、除外する。
     */
    private Set<String> imageExceptedExtension = new HashSet<String>() {
        {
            add("wbmp");
        }
    };
}

package please.change.me.common.captcha;

import nablarch.core.ThreadContext;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.MockHttpRequest;
import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.db.helper.DatabaseTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * {@link CaptchaGenerateHandler}のテスト
 *
 * @author TIS
 */
@RunWith(DatabaseTestRunner.class)
public class CaptchaGenerateHandlerTest extends CaptchaDbTestSupport {

    /**
     * CaptchaDummyGeneratorTest.xml を読み込む
     */
    @ClassRule
    public static final SystemRepositoryResource RESOURCE = new SystemRepositoryResource(
            "please/change/me/common/captcha/CaptchaDummyGeneratorTest.xml");

    /**
     * クラス初期化時の処理
     *
     * @throws Exception 例外
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        setupDb();
    }

    /**
     * ケース開始時の処理
     *
     * @throws Exception 例外
     */
    @Before
    public void setup() throws Exception {
        ThreadContext.setLanguage(Locale.getDefault());
        DbConnectionContext.setConnection(getTmConn());
    }

    /**
     * ケース終了時の処理
     *
     * @throws Exception 例外
     */
    @After
    public void tearDown() throws Exception {
        DbConnectionContext.removeConnection();
        // 未コミットのものは全てロールバック
        rollbackBizTran();
        terminateTmConn();
    }

    /**
     * 正常系テスト
     *
     * @throws Exception 例外
     */
    @Test
    public void testNormal() throws Exception {
        CaptchaGenerateHandler handler = new CaptchaGenerateHandler();
        ExecutionContext context = new ExecutionContext();
        HttpRequest request = new MockHttpRequest();
        request.setParam("captchaKey", CaptchaUtil.generateKey());

        HttpResponse response = handler.handle(request, context);
        commitBizTran();

        // ステータスコード
        assertEquals(200, response.getStatusCode());

        // 画像データを検証
        InputStream is = response.getBodyStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = is.read()) != -1) {
            baos.write(b);
        }
        assertArrayEquals("DUMMY_IMAGE".getBytes(), baos.toByteArray());
    }

    /**
     * 識別キー指定なし
     *
     * @throws Exception 例外
     */
    @Test
    public void testNoKey() throws Exception {
        CaptchaGenerateHandler handler = new CaptchaGenerateHandler();
        ExecutionContext context = new ExecutionContext();
        HttpRequest request = new MockHttpRequest();

        HttpResponse response;
        try {
            response = handler.handle(request, context);
            fail();
        } catch (HttpErrorResponse e) {
            response = e.getResponse();
        }

        // ステータスコードはエラー
        assertEquals(400, response.getStatusCode());

        // ボディ部は空
        InputStream is = response.getBodyStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // setCaptchaKeyのカバレッジのためのコード
        CaptchaForm form = new CaptchaForm(new HashMap<String, Object>());
        form.setCaptchaKey("hoge");

        int b;
        while ((b = is.read()) != -1) {
            baos.write(b);
        }
        assertEquals(0, baos.toByteArray().length);

    }

    /**
     * 識別キーが不正
     *
     * @throws Exception 例外
     */
    @Test
    public void testInvalidKey() throws Exception {
        CaptchaGenerateHandler handler = new CaptchaGenerateHandler();
        ExecutionContext context = new ExecutionContext();
        HttpRequest request = new MockHttpRequest();
        request.setParam("captchaKey", "全角の場合");

        HttpResponse response;
        try {
            response = handler.handle(request, context);
            fail();
        } catch (HttpErrorResponse e) {
            response = e.getResponse();
        }

        // ステータスコードはエラー
        assertEquals(400, response.getStatusCode());

        // ボディ部は空
        InputStream is = response.getBodyStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = is.read()) != -1) {
            baos.write(b);
        }
        assertEquals(0, baos.toByteArray().length);
    }

    /**
     * 識別キーが２つ
     *
     * @throws Exception 例外
     */
    @Test
    public void testMultiKey() throws Exception {
        CaptchaGenerateHandler handler = new CaptchaGenerateHandler();
        ExecutionContext context = new ExecutionContext();

        // 先頭エラー値
        HttpRequest request = new MockHttpRequest();
        request.setParam("captchaKey", "a", CaptchaUtil.generateKey());

        HttpResponse response;
        try {
            response = handler.handle(request, context);
            fail();
        } catch (HttpErrorResponse e) {
            response = e.getResponse();
        }

        // ステータスコードはエラー
        assertEquals(400, response.getStatusCode());

        // ボディ部は空
        InputStream is = response.getBodyStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = is.read()) != -1) {
            baos.write(b);
        }
        assertEquals(0, baos.toByteArray().length);

        // 先頭に正常値（生成キー）
        request = new MockHttpRequest();
        request.setParam("captchaKey", CaptchaUtil.generateKey());
        response = handler.handle(request, context);
        commitBizTran();

        // ステータスコード
        assertEquals(200, response.getStatusCode());

        // 画像データを検証
        is = response.getBodyStream();
        baos = new ByteArrayOutputStream();
        while ((b = is.read()) != -1) {
            baos.write(b);
        }
        assertArrayEquals("DUMMY_IMAGE".getBytes(), baos.toByteArray());
    }

    /**
     * 画像生成でエラー
     *
     * @throws Exception 例外
     */
    @Test
    public void testGenerateImageError() throws Exception {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "please/change/me/common/captcha/CaptchaErrorGeneratorTest.xml");
        SystemRepository.load(new DiContainer(loader));

        CaptchaGenerateHandler handler = new CaptchaGenerateHandler();
        ExecutionContext context = new ExecutionContext();
        HttpRequest request = new MockHttpRequest();
        request.setParam("captchaKey", "a");

        HttpResponse response;
        try {
            response = handler.handle(request, context);
            fail();
        } catch (HttpErrorResponse e) {
            response = e.getResponse();
        }

        // ステータスコードはエラー
        assertEquals(500, response.getStatusCode());

        // ボディ部は空
        InputStream is = response.getBodyStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = is.read()) != -1) {
            baos.write(b);
        }
        assertEquals(0, baos.toByteArray().length);

        // リポジトリを戻す
        loader = new XmlComponentDefinitionLoader("please/change/me/common/captcha/CaptchaDummyGeneratorTest.xml");
        SystemRepository.load(new DiContainer(loader));
    }
}

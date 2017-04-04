package please.change.me.common.captcha;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import nablarch.core.date.SystemTimeUtil;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.test.RepositoryInitializer;
import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.db.helper.DatabaseTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * {@link CaptchaUtil}のテスト
 * 
 * @author TIS
 */
@RunWith(DatabaseTestRunner.class)
public class CaptchaUtilTest extends CaptchaDbTestSupport {
    /** UUID パターン */
    private static final Pattern UUID_PATTERN = Pattern.compile("[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}");

    /**
     * CaptchaTest.xml を読み込む
     */
    @ClassRule
    public static final SystemRepositoryResource RESOURCE = new SystemRepositoryResource(
            "please/change/me/common/captcha/CaptchaTest.xml");

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
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
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
     * デフォルトのgeneratorを使った画像生成
     * 
     * @throws SQLException 例外
     */
    @Test
    public void testDefaultGenerator() throws SQLException {
        String key = CaptchaUtil.generateKey();
        Captcha captcha = CaptchaUtil.generateImage(key);
        
        // 全ての項目が設定されていることを確認（ここでは一部の内容はわからないので具体値の検証はしない）
        assertNotNull(captcha);
        assertTrue(UUID_PATTERN.matcher(captcha.getKey()).matches()); // UUID形式であること
        assertEquals(SystemTimeUtil.getDate(), captcha.getGenerateDateTime()); // systemTimeProviderがFixedSystemTimeProviderだと同じであるはず
        
        assertNotNull(captcha.getText());
        assertNotNull(captcha.getImage());
        
        commitBizTran();
        
        // 1件のみ登録されていることを確認
        assertEquals(1, countManageTableByKey(captcha.getKey()));
    }

    /**
     * ダミーのgeneratorを使った画像生成
     * 
     * @throws SQLException 例外
     */
    @Test
    public void testDummyGenerator() throws SQLException {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "please/change/me/common/captcha/CaptchaDummyGeneratorTest.xml");
        SystemRepository.load(new DiContainer(loader));
        
        String key = CaptchaUtil.generateKey();
        Captcha captcha = CaptchaUtil.generateImage(key);
        
        // 全ての項目が設定されていることを確認
        assertNotNull(captcha);
        assertTrue(UUID_PATTERN.matcher(captcha.getKey()).matches()); // UUID形式であること
        assertEquals(SystemTimeUtil.getDate(), captcha.getGenerateDateTime()); // systemTimeProviderがFixedSystemTimeProviderだと同じであるはず
        
        assertEquals("DUMMY_TEXT", captcha.getText());
        assertArrayEquals("DUMMY_IMAGE".getBytes(), captcha.getImage());
        
        commitBizTran();
        
        // 1件のみ登録されていることを確認
        assertEquals(1, countManageTableByKey(captcha.getKey()));
    }

    /**
     * 認証処理の正常系テスト
     */
    @Test
    public void testAuthenticate() {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "please/change/me/common/captcha/CaptchaDummyGeneratorTest.xml");
        SystemRepository.load(new DiContainer(loader));
        
        String key = CaptchaUtil.generateKey();
        Captcha captcha = CaptchaUtil.generateImage(key);
        
        // 認証OK
        assertTrue(CaptchaUtil.authenticate(captcha.getKey(), "DUMMY_TEXT"));
        // 認証NG
        assertFalse(CaptchaUtil.authenticate(captcha.getKey(), "XXXXX"));
    }

    /**
     * 認証処理の異常系テスト(Generatorが取得できない場合)
     */
    @Test
    public void testAuthenticateInvalidRepository() {
        RepositoryInitializer.reInitializeRepository("please/change/me/common/captcha/CaptchaTest.xml");
        String key = CaptchaUtil.generateKey();
        Captcha captcha = CaptchaUtil.generateImage(key);
        
        // 認証NG
        assertFalse(CaptchaUtil.authenticate(captcha.getKey(), "DUMMY_TEXT"));
    }
    
    /**
     * 認証処理の引数異常系テスト
     */
    @Test
    public void testAuthenticateInvalidArg() throws SQLException {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "please/change/me/common/captcha/CaptchaDummyGeneratorTest.xml");
        SystemRepository.load(new DiContainer(loader));
        
        // 入力値がnull
        assertFalse(CaptchaUtil.authenticate("hoge", null));
        
        // キーがnull
        assertFalse(CaptchaUtil.authenticate(null, "DUMMY"));
        
        // 存在しないキー
        assertFalse(CaptchaUtil.authenticate("hoge", "DUMMY"));
    }
    
    
    private ExecutorService service;
    
    /**
     * マルチスレッド下で動作することを確認する。
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void testMultiThread() throws InterruptedException, ExecutionException {

        service = Executors.newFixedThreadPool(100);
        final String key = CaptchaUtil.generateKey();

        final int cnt = 10000;
        List<Callable<Captcha>> callables = new ArrayList<Callable<Captcha>>(cnt);

        for (int i = 0; i < cnt; i++) {
            callables.add(new Callable<Captcha>() {
                @Override
                public Captcha call() throws Exception {
                    return CaptchaUtil.generateImage(key);
                }
            });
        }

        List<Future<Captcha>> futures = service.invokeAll(callables);
        service.shutdownNow();
        // 呼び出し回数と結果数が一致していること。
        assertThat(futures.size(), is(cnt));

    }
}

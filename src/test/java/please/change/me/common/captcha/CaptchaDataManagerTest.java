package please.change.me.common.captcha;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Date;

import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.statement.exception.DuplicateStatementException;
import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.db.helper.DatabaseTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * {@link CaptchaDataManager}のテスト
 * 
 * @author TIS
 */
@RunWith(DatabaseTestRunner.class)
public class CaptchaDataManagerTest extends CaptchaDbTestSupport {

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
        // 消す
        deleteFromManageTable();
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
     * レコード作成処理のテスト
     * 
     * @throws Exception 例外
     */
    @Test
    public void testCreate() throws Exception {
        CaptchaDataManager manager = new CaptchaDataManager();
        manager.create("a");
        
        // 業務トランザクションのコミット前は別トランザクションで取得できない
        // (内部でトランザクション制御していないことの検証)
        assertEquals(0, countManageTableByKey("a"));
        
        commitBizTran();
        
        // 業務トランザクションのコミット後は別トランザクションで取得できる
        assertEquals(1, countManageTableByKey("a"));
    }
    
    /**
     * レコード作成処理の２重登録テスト
     * 
     * @throws Exception 例外
     */
    @Test
    public void testCreateDuplicate() throws Exception {
        CaptchaDataManager manager = new CaptchaDataManager();
        manager.create("a");
        commitBizTran();
        
        try {
            manager.create("a");
            fail();
        } catch (Exception e) {
            assertThat(e, is(instanceOf(DuplicateStatementException.class)));
        }
    }
    
    /**
     * 保存処理のテスト
     * 
     * @throws Exception 例外
     */
    @Test
    public void testSave() throws Exception {
        // 識別キーを登録
        insertIntoManageTableByKey(new Captcha().setKey("a"));

        Date generateDateTime = new Date();
        Captcha captcha = new Captcha()
                            .setKey("a")
                            .setText("b")
                            .setGenerateDateTime(generateDateTime);
        
        CaptchaDataManager manager = new CaptchaDataManager();
        manager.save(captcha);
        
        // 業務トランザクションのコミット前は別トランザクションで取得できない
        // (内部でトランザクション制御していないことの検証)
        assertEquals(1, countManageTableByKey("a"));
        Captcha insertedCaptcha = getFromManageTableByKey("a");
        assertNull(insertedCaptcha.getText());
        assertNull(insertedCaptcha.getGenerateDateTime());
        
        commitBizTran();
        
        // 業務トランザクションのコミット後は別トランザクションで取得できる
        assertEquals(1, countManageTableByKey("a"));
        insertedCaptcha = getFromManageTableByKey("a");
        assertEquals("a", insertedCaptcha.getKey());
        assertEquals("b", insertedCaptcha.getText());
        assertEquals(generateDateTime, insertedCaptcha.getGenerateDateTime());
        
    }

    /**
     * 保存処理の引数異常テスト
     * 
     * @throws Exception 例外
     */
    @Test
    public void testSaveInvalidArg() throws Exception {
        CaptchaDataManager manager = new CaptchaDataManager();
        
        try {
            manager.save(null);
            fail();
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IllegalArgumentException.class)));
            assertEquals("captcha is null.", e.getMessage());
        }
        
        try {
            manager.save(new Captcha());
            fail();
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IllegalArgumentException.class)));
            assertEquals("captcha.key is null.", e.getMessage());
        }
        
        try {
            manager.save(new Captcha().setKey("a"));
            fail();
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IllegalArgumentException.class)));
            assertEquals("captcha.text is null.", e.getMessage());
        }
        
        try {
            manager.save(new Captcha().setKey("a").setText("b"));
            fail();
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IllegalArgumentException.class)));
            assertEquals("captcha.generateDateTime is null.", e.getMessage());
        }
        
        assertFalse(manager.save(new Captcha().setKey("errkey").setText("b").setGenerateDateTime(new Date())));
}
    
    /**
     * 読込処理のテスト
     * 
     * @throws Exception 例外
     */
    @Test
    public void testLoad() throws Exception {

        CaptchaDataManager manager = new CaptchaDataManager();
        Captcha captcha = manager.load("c");
        assertNull(captcha);

        // 別トランザクションでの作成
        Date generateDateTime = new Date();
        Captcha insertCaptcha = new Captcha()
                                .setKey("c")
                                .setText("d")
                                .setGenerateDateTime(generateDateTime);
        insertIntoManageTableByKey(insertCaptcha);

        // 別トランザクションでも、コミット後は業務トランザクションで取得できる
        captcha = manager.load("c");
        assertNotNull(captcha);
        assertEquals("c", captcha.getKey());
        assertEquals("d", captcha.getText());
        assertEquals(generateDateTime, captcha.getGenerateDateTime());
    }
    /**
     * 読込処理の引数異常テスト
     * 
     * @throws Exception 例外
     */
    @Test
    public void testLoadInvalidArg() throws Exception {
        CaptchaDataManager manager = new CaptchaDataManager();
        
        try {
            manager.load(null);
            fail();
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IllegalArgumentException.class)));
            assertEquals("key is null.", e.getMessage());
        }
    }

}

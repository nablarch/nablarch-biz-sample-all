package please.change.me.common.captcha;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;

import java.util.Date;

import nablarch.core.db.statement.exception.DuplicateStatementException;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.test.RepositoryInitializer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * {@link CaptchaDataManager}のテスト
 * 
 * @author TIS
 */
public class CaptchaDataManagerTest extends CaptchaDbTestSupport {

    /**
     * クラス初期化時の処理
     * 
     * @throws Exception 例外
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader("please/change/me/common/captcha/CaptchaTest.xml");
        SystemRepository.load(new DiContainer(loader));

        setupDb();
    }

    /**
     * クラス終了時の処理
     * 
     * @throws Exception 例外
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        teardownDb();
        
        RepositoryInitializer.initializeDefaultRepository();
    }

    /**
     * ケース終了時の処理
     * 
     * @throws Exception 例外
     */
    @After
    public void tearDown() throws Exception {
        // 未コミットのものは全てロールバック
        rollbackBizTran();
        rollbackTestTran();
    }

    /**
     * レコード作成処理のテスト
     * 
     * @throws Exception 例外
     */
    @Test
    public void testCreate() throws Exception {
        // 一応消す
        deleteFromManageTableByKey("a");
        commitTestTran();
        
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
        // 一応消す
        deleteFromManageTableByKey("a");
        commitTestTran();
        
        CaptchaDataManager manager = new CaptchaDataManager();
        manager.create("a");
        commitBizTran();
        
        try {
            manager.create("a");
            fail();
        } catch (Exception e) {
            assertThat(e, is(DuplicateStatementException.class));
        }
    }
    
    /**
     * 保存処理のテスト
     * 
     * @throws Exception 例外
     */
    @Test
    public void testSave() throws Exception {
        // 一応消す
        deleteFromManageTableByKey("a");
        commitTestTran();
        
        // 識別キーを登録
        insertIntoManageTableByKey(new Captcha().setKey("a"));
        commitTestTran();
        
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
            assertThat(e, is(IllegalArgumentException.class));
            assertEquals("captcha is null.", e.getMessage());
        }
        
        try {
            manager.save(new Captcha());
            fail();
        } catch (Exception e) {
            assertThat(e, is(IllegalArgumentException.class));
            assertEquals("captcha.key is null.", e.getMessage());
        }
        
        try {
            manager.save(new Captcha().setKey("a"));
            fail();
        } catch (Exception e) {
            assertThat(e, is(IllegalArgumentException.class));
            assertEquals("captcha.text is null.", e.getMessage());
        }
        
        try {
            manager.save(new Captcha().setKey("a").setText("b"));
            fail();
        } catch (Exception e) {
            assertThat(e, is(IllegalArgumentException.class));
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
        
        // 一応消す
        deleteFromManageTableByKey("c");
        commitTestTran();
        
        Date generateDateTime = new Date();
        
        Captcha insertCaptcha = new Captcha()
                                .setKey("c")
                                .setText("d")
                                .setGenerateDateTime(generateDateTime);
        
        insertIntoManageTableByKey(insertCaptcha);
        
        CaptchaDataManager manager = new CaptchaDataManager();
        Captcha captcha = manager.load("c");
        
        // 別トランザクションのコミット前は業務トランザクションで取得できない
        // (対象が存在しない場合はnullが返却される)
        assertNull(captcha);
        
        commitTestTran();
        
        // 別トランザクションのコミット後は業務トランザクションで取得できる
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
            assertThat(e, is(IllegalArgumentException.class));
            assertEquals("key is null.", e.getMessage());
        }
    }

}

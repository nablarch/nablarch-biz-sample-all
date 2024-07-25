package please.change.me.common.mail.html;

import nablarch.core.db.transaction.SimpleDbTransactionManager;
import nablarch.core.repository.SystemRepository;
import nablarch.test.RepositoryInitializer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;


/**
 * Mail機能のテストサポートクラス。<br />
 *
 * 利用するコンポーネント定義を解決する。
 *
 * @author tani takanori
 */
public class HtmlMailTestSupport {

    private final SimpleDbTransactionManager transactionManager = SystemRepository.get("dbManager-default");

    /**
     * 利用するコンポーネント定義ファイルのパス。
     * @see #setupRepository()
     */
    private static final String COMPORNENT_DEFINITION = "please/change/me/common/mail/html/mail-test.xml";

    /**
     * コンポーネント定義で定義した情報を基に、{@link RepositoryInitializer#reInitializeRepository(String)}を行う。<br>
     * テーブルの初期化も行う。
     *
     * @throws Exception 想定外の例外
     */
    @BeforeClass
    public static void setupRepository() throws Exception {
        RepositoryInitializer.reInitializeRepository(COMPORNENT_DEFINITION);
        HtmlMailTestDbSupport.initDB();
    }

    /**
     * テーブルの削除を行う。
     */
    @AfterClass
    public static void tearDownClass() {
        HtmlMailTestDbSupport.clearDb();
    }

    /**
     * トランザクションを開始する。
     */
    @Before
    public void setUp() {
        transactionManager.beginTransaction();
    }

    /**
     * トランザクションをコミットする。
     */
    protected void commit() {
        transactionManager.commitTransaction();
    }

    /**
     * トランザクションを終了する。
     */
    @After
    public void tearDown() {
        transactionManager.endTransaction();
    }

    /**
     * デフォルトのリポジトリに戻す。<br>
     * 使用したコネクションをクローズする。
     *
     * @throws Exception コネクションのクローズに失敗した場合
     */
    @AfterClass
    public static void revertRepository() throws Exception {
        RepositoryInitializer.initializeDefaultRepository();
    }
}

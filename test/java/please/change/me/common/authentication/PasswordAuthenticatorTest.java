package please.change.me.common.authentication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.ResourceBundle;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.transaction.SimpleDbTransactionManager;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import oracle.jdbc.pool.OracleDataSource;
import please.change.me.util.FixedBusinessDateProvider;
import please.change.me.util.FixedSystemTimeProvider;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link PasswordAuthenticator}のテストクラス。
 *
 * @author Kiyohito Itoh
 */
public class PasswordAuthenticatorTest {

    /** テストデータなどをセットアップするためのコネクション */
    private static Connection con;

    /** パスワード暗号化コンポーネント */
    private static PasswordEncryptor encryptor = getEncryptor();

    /**
     * 必要な初期設定を行った {@link PasswordEncryptor} を返却する。
     *
     * @return 必要な初期設定を行った {@link PasswordEncryptor}
     */
    private static PasswordEncryptor getEncryptor() {
        PBKDF2PasswordEncryptor encryptor = new PBKDF2PasswordEncryptor();
        encryptor.setFixedSalt("fixedSaltString");
        return encryptor;
    }

    /** xml（テスト用設定ファイル）の配置ディレクトリ */
    private static final String COMPONENT_BASE_PATH = "please/change/me/common/authentication/";

    /**
     * セットアップ。
     *
     * テスト時に使用するデータベース接続の生成及びテスト用のテーブルのセットアップを行う。
     *
     * @throws SQLException 例外
     */
    @BeforeClass
    public static void classSetup() throws SQLException {

        ResourceBundle rb = ResourceBundle.getBundle("db-config");
        OracleDataSource ds = new OracleDataSource();
        ds.setURL(rb.getString("db.url"));
        ds.setUser(rb.getString("db.user"));
        ds.setPassword(rb.getString("db.password"));
        con = ds.getConnection();

        // setup test table
        Statement statement = con.createStatement();
        try {
            statement.execute("DROP TABLE SYSTEM_ACCOUNT CASCADE CONSTRAINTS");
        } catch (Exception e) {
            // nop
        }

        statement.execute("CREATE TABLE SYSTEM_ACCOUNT("
                + " USER_ID                   CHAR(10) NOT NULL,"
                + " PASSWORD                  VARCHAR2(128) NOT NULL,"
                + " USER_ID_LOCKED            CHAR(1) DEFAULT 0 NOT NULL,"
                + " PASSWORD_EXPIRATION_DATE  CHAR(8) DEFAULT '99991231' NOT NULL,"
                + " FAILED_COUNT              NUMBER(1) DEFAULT 0 NOT NULL,"
                + " EFFECTIVE_DATE_FROM       CHAR(8) DEFAULT '19000101' NOT NULL,"
                + " EFFECTIVE_DATE_TO         CHAR(8) DEFAULT '99991231' NOT NULL,"
                + " LAST_LOGIN_DATE_TIME      TIMESTAMP)");
        statement.execute("ALTER TABLE SYSTEM_ACCOUNT ADD CONSTRAINT PK_sa"
                + " PRIMARY KEY (USER_ID)");



        statement.close();

        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                COMPONENT_BASE_PATH + "authentication-db.xml");
        SystemRepository.load(new DiContainer(loader));
    }

    @Before
    public void setUp() throws Exception {
        DbConnectionContext.removeConnection();

        PreparedStatement truncate = con.prepareStatement("truncate table system_account");
        truncate.execute();
        truncate.close();

        // テストデータのセットアップ
        PreparedStatement insert = con.prepareStatement(
                "insert into SYSTEM_ACCOUNT values (?, ?, ?, ?, ?, ?, ?, ?)");
        // active user
        insert.setString(1, "0000000001");
        insert.setString(2, encryptor.encrypt("0000000001", "password"));
        insert.setString(3, "0");
        insert.setString(4, "20130804");
        insert.setInt(5, 0);
        insert.setString(6, "20130802");
        insert.setString(7, "20130805");
        insert.setNull(8, Types.TIMESTAMP);
        insert.execute();

        // locked user
        insert.setString(1, "0000000003");
        insert.setString(2, encryptor.encrypt("0000000001", "password"));
        insert.setString(3, "1");       // locked
        insert.setString(4, "20130804");
        insert.setInt(5, 0);
        insert.setString(6, "20130802");
        insert.setString(7, "20130805");
        insert.setNull(8, Types.TIMESTAMP);
        insert.execute();

        insert.setString(1, "0000000004");
        insert.setString(2, encryptor.encrypt("0000000004", "password"));
        insert.setString(3, "0");
        insert.setString(4, "20130804");
        insert.setInt(5, 2);
        insert.setString(6, "20130802");
        insert.setString(7, "20130805");
        insert.setNull(8, Types.TIMESTAMP);
        insert.execute();

        insert.setString(1, "0000000005");
        insert.setString(2, encryptor.encrypt("0000000005", "pass!!!"));
        insert.setString(3, "0");
        insert.setString(4, "20130805");
        insert.setInt(5, 0);
        insert.setString(6, "20130802");
        insert.setString(7, "20130805");
        insert.setNull(8, Types.TIMESTAMP);
        insert.execute();

        insert.close();
        con.commit();
    }


    /**
     * クラス終了時の処理。
     *
     * @throws Exception 例外
     */
    @AfterClass
    public static void classDown() throws Exception {
        if (con != null) {
            con.close();
        }
        SystemRepository.clear();
    }

    /**
     * 認可対象のユーザIDがnullの場合は、{@link AuthenticationFailedException}が送出されること。
     */
    @Test
    public void testUserIdIsNull() {
        PasswordAuthenticator authenticator = createPasswordAuthenticator("20130802");

        try {
            authenticator.authenticate(null, "password");
            fail("エラーが発生するので、ここは通過しない");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getUserId(), is(nullValue()));
        }
    }

    /**
     * 認可対象のユーザIDが空文字列の場合は、{@link AuthenticationFailedException}が送出されること。
     */
    @Test
    public void testUserIdIsEmptyString() {
        PasswordAuthenticator authenticator = createPasswordAuthenticator("20130802");

        try {
            authenticator.authenticate("", "password");
            fail("エラーが発生するので、ここは通過しない");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getUserId(), is(""));
        }
    }

    /**
     * 認可対象のユーザIDがデータベースに存在しないので、{@link AuthenticationFailedException}が送出されること。
     */
    @Test
    public void testUserIsNotFound() {

        PasswordAuthenticator authenticator = createPasswordAuthenticator("20130802");

        try {
            authenticator.authenticate("0000000002", "password");
            fail("エラーが発生するので、ここは通過しない");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getUserId(), is("0000000002"));
        }
    }

    /**
     * 認可対象のユーザIDが存在しているが、有効期限が到来していない場合は{@link AuthenticationFailedException}が送出されること。
     */
    @Test
    public void testEffectiveDateIsNonArrival() {

        PasswordAuthenticator authenticator = createPasswordAuthenticator("20130801");

        try {
            authenticator.authenticate("0000000001", "password");
            fail("エラーが発生するので、ここは通過しない");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getUserId(), is("0000000001"));
        }
    }

    /**
     * 認可対象のユーザIDが存在しているが、有効期限を超過している場合は{@link AuthenticationFailedException}が送出されること。
     */
    @Test
    public void testEffectiveDateIsOver() {
        PasswordAuthenticator authenticator = createPasswordAuthenticator("20130806");

        try {
            authenticator.authenticate("0000000001", "password");
            fail("エラーが発生するので、ここは通過しない");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getUserId(), is("0000000001"));
        }
    }

    /**
     * 認可対象のパスワードがnullなので、{@link AuthenticationFailedException}が送出されること。
     */
    @Test
    public void testPasswordIsNull() {
        PasswordAuthenticator authenticator = createPasswordAuthenticator("20130802");

        try {
            authenticator.authenticate("0000000001", null);
            fail("エラーが発生するので、ここは通過しない");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getUserId(), is("0000000001"));
        }
    }

    /**
     * 認可対象のパスワードが空文字列なので、{@link AuthenticationFailedException}が送出されること。
     */
    @Test
    public void testPasswordIsEmptyString() {
        PasswordAuthenticator authenticator = createPasswordAuthenticator("20130802");

        try {
            authenticator.authenticate("0000000001", "");
            fail("エラーが発生するので、ここは通過しない");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getUserId(), is("0000000001"));
        }
    }

    /**
     * 認可対象のユーザIDが存在しているが、パスワードが一致しないため{@link AuthenticationFailedException}が送出されること。
     */
    @Test
    public void testPasswordIsUnMatch() {
        PasswordAuthenticator authenticator = createPasswordAuthenticator("20130802");

        try {
            authenticator.authenticate("0000000001", "password1");
            fail("エラーが発生するので、ここは通過しない");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getUserId(), is("0000000001"));
        }
    }

    /**
     * パスワードの有効期限切れの場合は、{@link PasswordExpiredException}が送出されること。
     */
    @Test
    public void testPasswordExpiryIsOver() {
        PasswordAuthenticator authenticator = createPasswordAuthenticator("20130805");
        authenticator.setFailedCountToLock(0);

        try {
            authenticator.authenticate("0000000001", "password");
            fail("エラーが発生するので、ここは通過しない");
        } catch (PasswordExpiredException e) {
            assertThat(e.getUserId(), is("0000000001"));
            assertThat(e.getPasswordExpirationDate(), is("20130804"));
            assertThat(e.getBusinessDate(), is("20130805"));
        }
    }

    /**
     * 対象のユーザがロックされている場合は、{@link UserIdLockedException}が送出されること。
     */
    @Test
    public void testAccountLocked() {
        PasswordAuthenticator authenticator = createPasswordAuthenticator("20130802");
        authenticator.setFailedCountToLock(5);

        try {
            authenticator.authenticate("0000000003", "password");
            fail("エラーが発生するので、ここは通過しない");
        } catch (UserIdLockedException e) {
            assertThat(e.getUserId(), is("0000000003"));
            assertThat(e.getFailedCountToLock(), is(5));
        }
    }

    /**
     * ログイン成功の場合、最終ログイン日時が更新されること。
     */
    @Test
    public void testLoginSuccess() throws Exception {

        //**********************************************************************
        // 業務日付がユーザの有効期限（開始日）と同日
        //**********************************************************************
        createPasswordAuthenticator("20130802").authenticate("0000000005", "pass!!!");

        PreparedStatement statement = con.prepareStatement("select * from system_account where user_id = ?");
        statement.setString(1, "0000000005");
        ResultSet resultSet = statement.executeQuery();
        assertThat(resultSet.next(), is(true));
        assertThat("ユーザはロック中のまま", resultSet.getString("USER_ID_LOCKED"), is("0"));
        assertThat("失敗回数は変わらない", resultSet.getInt("FAILED_COUNT"), is(0));
        assertThat("最終ログイン日時が更新されること", resultSet.getTimestamp("LAST_LOGIN_DATE_TIME"),
                is(Timestamp.valueOf("2013-08-23 00:11:22.000")));

        //**********************************************************************
        // 業務日付がユーザの有効期限（終了日）と同日
        //**********************************************************************
        createPasswordAuthenticator("20130805").authenticate("0000000005", "pass!!!");
    }

    /**
     * 対象のユーザが未ロックだが、認証失敗しロックされた場合{@link UserIdLockedException}が送出されること。
     * また、対象のユーザのロック状態が未ロックからロックに変更されること。
     *
     * 以下の順でテストを実施する。
     * <ol>
     *     <li>認証が成功することを確認</li>
     *     <li>認証失敗でアカウントがロックされ、認証失敗回数がインクリメントされる。</li>
     *     <li>認証成功するがアカウントがロックされているので、ロック中例外</li>
     * </ol>
     */
    @Test
    public void testFailedAndAccountLock() throws Exception {
        PasswordAuthenticator authenticator = createPasswordAuthenticator("20130802");
        authenticator.setFailedCountToLock(3);

        //**********************************************************************
        // 認証が成功する(認証回数リセット)
        //**********************************************************************
        authenticator.authenticate("0000000004", "password");

        // assertion
        PreparedStatement statement1 = con.prepareStatement("select * from system_account where user_id = ?");
        statement1.setString(1, "0000000004");
        ResultSet resultSet1 = statement1.executeQuery();

        assertThat(resultSet1.next(), is(true));
        assertThat("認証失敗回数は0", resultSet1.getInt("FAILED_COUNT"), is(0));

        statement1.close();

        //**********************************************************************
        // 認証が失敗する(3回失敗するとロックされる)
        //**********************************************************************
        try {
            // 1回目
            authenticator.authenticate("0000000004", "password un match");
            fail("エラーが発生するので、ここは通過しない");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getUserId(), is("0000000004"));
        }
        try {
            // 2回目
            authenticator.authenticate("0000000004", "password un match");
            fail("エラーが発生するので、ここは通過しない");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getUserId(), is("0000000004"));
        }
        try {
            // 3回目
            authenticator.authenticate("0000000004", "password un match");
            fail("エラーが発生するので、ここは通過しない");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getUserId(), is("0000000004"));
        }

        // assertion
        PreparedStatement statement2 = con.prepareStatement("select * from system_account where user_id = ?");
        statement2.setString(1, "0000000004");
        ResultSet resultSet2 = statement2.executeQuery();

        assertThat(resultSet2.next(), is(true));
        assertThat("ユーザがロック済('1')みに変更されること", resultSet2.getString("USER_ID_LOCKED"), is("1"));
        assertThat("認証失敗回数がインクリメントされること", resultSet2.getInt("FAILED_COUNT"), is(3));

        statement2.close();

        //**********************************************************************
        // 認証成功するがユーザロック中
        //**********************************************************************
        try {
            authenticator.authenticate("0000000004", "password");
            fail("エラーが発生するので、ここは通過しない");
        } catch (UserIdLockedException e) {
            assertThat(e.getUserId(), is("0000000004"));
            assertThat(e.getFailedCountToLock(), is(3));
        }

        PreparedStatement statement3 = con.prepareStatement("select * from system_account where user_id = ?");
        statement3.setString(1, "0000000004");
        ResultSet resultSet3 = statement3.executeQuery();

        assertThat(resultSet3.next(), is(true));
        assertThat("ユーザはロック中のまま", resultSet3.getString("USER_ID_LOCKED"), is("1"));
        assertThat("失敗回数は変わらない", resultSet3.getInt("FAILED_COUNT"), is(3));

        statement3.close();
    }

    /**
     * ユーザのロック機能を使用しない（認証エラー上限回数が0の場合）
     */
    @Test
    public void testUnsupportedUserLock() throws Exception {
        PasswordAuthenticator authenticator = createPasswordAuthenticator("20130802");
        // ロックされる上限回数を0に設定
        authenticator.setFailedCountToLock(0);

        //*********************************************************************
        // 認証成功
        //*********************************************************************
        authenticator.authenticate("0000000004", "password");

        PreparedStatement statement1 = con.prepareStatement("select * from system_account where user_id = ?");
        statement1.setString(1, "0000000004");
        ResultSet resultSet1 = statement1.executeQuery();

        assertThat(resultSet1.next(), is(true));
        assertThat("パスワード失敗回数は変更されない", resultSet1.getInt("FAILED_COUNT"), is(2));

        statement1.close();

        //*********************************************************************
        // 認証失敗
        //*********************************************************************
        try {
            authenticator.authenticate("0000000004", "passwor");
            fail("エラーが発生するので、ここは通過しない");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getUserId(), is("0000000004"));
        }

        PreparedStatement statement2 = con.prepareStatement("select * from system_account where user_id = ?");
        statement2.setString(1, "0000000004");
        ResultSet resultSet2 = statement2.executeQuery();

        assertThat(resultSet2.next(), is(true));
        assertThat("パスワード失敗回数は変更されない", resultSet2.getInt("FAILED_COUNT"), is(2));

        statement2.close();
    }

    /**
     * テスト対象の{@link PasswordAuthenticator}を生成する。
     *
     * @param businessDate 業務日付
     *
     * @return 生成した {@link PasswordAuthenticator}
     */
    private PasswordAuthenticator createPasswordAuthenticator(final String businessDate) {
        PasswordAuthenticator authenticator = new PasswordAuthenticator();

        authenticator.setFailedCountToLock(1);

        authenticator.setDbManager(SystemRepository.<SimpleDbTransactionManager>get("dbManager"));
        authenticator.setPasswordEncryptor(encryptor);

        FixedBusinessDateProvider businessDateProvider = new FixedBusinessDateProvider();
        businessDateProvider.setFixedDate(
                new HashMap<String, String>() {
                    {
                        put("00", businessDate);
            } }
        );
        businessDateProvider.setDefaultSegment("00");
        authenticator.setBusinessDateProvider(businessDateProvider);

        FixedSystemTimeProvider systemTimeProvider = new FixedSystemTimeProvider();
        systemTimeProvider.setFixedDate("20130823001122");
        authenticator.setSystemTimeProvider(systemTimeProvider);

        return authenticator;
    }
}


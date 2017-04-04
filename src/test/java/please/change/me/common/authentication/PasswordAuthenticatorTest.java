package please.change.me.common.authentication;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.util.HashMap;

import please.change.me.common.authentication.entity.SystemAccount;
import please.change.me.util.FixedBusinessDateProvider;
import please.change.me.util.FixedSystemTimeProvider;

import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.transaction.SimpleDbTransactionManager;
import nablarch.core.repository.SystemRepository;
import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.db.helper.DatabaseTestRunner;
import nablarch.test.support.db.helper.VariousDbTestHelper;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * {@link PasswordAuthenticator}のテストクラス。
 *
 * @author Kiyohito Itoh
 */
@RunWith(DatabaseTestRunner.class)
public class PasswordAuthenticatorTest {

    @ClassRule
    public static final SystemRepositoryResource RESOURCE = new SystemRepositoryResource(
            "please/change/me/common/authentication/authentication-db.xml");

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

    /**
     * セットアップ。
     *
     * テスト時に使用するデータベース接続の生成及びテスト用のテーブルのセットアップを行う。
     *
     */
    @BeforeClass
    public static void classSetup() {
        VariousDbTestHelper.createTable(SystemAccount.class);
    }

    @Before
    public void setUp() throws Exception {
        DbConnectionContext.removeConnection();

        // active user
        VariousDbTestHelper.setUpTable(
                new SystemAccount(
                        "0000000001",
                        encryptor.encrypt("0000000001", "password"),
                        "0",
                        "20130804",
                        0,
                        "20130802",
                        "20130805",
                        null
                ),
                // locked user
                new SystemAccount(
                        "0000000003",
                        encryptor.encrypt("0000000001", "password"),
                        "1", // locked
                        "20130804",
                        0,
                        "20130802",
                        "20130805",
                        null
                ),
                new SystemAccount(
                        "0000000004",
                        encryptor.encrypt("0000000004", "password"),
                        "0",
                        "20130804",
                        2,
                        "20130802",
                        "20130805",
                        null
                ),
                new SystemAccount(
                        "0000000005",
                        encryptor.encrypt("0000000005", "pass!!!"),
                        "0",
                        "20130805",
                        0,
                        "20130802",
                        "20130805",
                        null
                )
        );
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

        SystemAccount account = VariousDbTestHelper.findById(SystemAccount.class, "0000000005");
        assertThat(account, notNullValue());
        assertThat("ユーザはロック中のまま", account.userIdLocked, is("0"));
        assertThat("失敗回数は変わらない", account.failedCount, is(0));
        assertThat("最終ログイン日時が更新されること", account.lastLoginDateTime,
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
        SystemAccount account1 = VariousDbTestHelper.findById(SystemAccount.class, "0000000004");
        assertThat( account1, notNullValue());
        assertThat("認証失敗回数は0", account1.failedCount, is(0));

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
        SystemAccount account2 = VariousDbTestHelper.findById(SystemAccount.class, "0000000004");
        assertThat(account2, notNullValue());
        assertThat("ユーザがロック済('1')みに変更されること", account2.userIdLocked, is("1"));
        assertThat("認証失敗回数がインクリメントされること", account2.failedCount, is(3));

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

        SystemAccount account3 = VariousDbTestHelper.findById(SystemAccount.class, "0000000004");
        assertThat(account3, notNullValue());
        assertThat("ユーザはロック中のまま", account3.userIdLocked, is("1"));
        assertThat("失敗回数は変わらない", account3.failedCount, is(3));
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

        SystemAccount account1 = VariousDbTestHelper.findById(SystemAccount.class, "0000000004");
        assertThat(account1, notNullValue());
        assertThat("パスワード失敗回数は変更されない", account1.failedCount, is(2));

        //*********************************************************************
        // 認証失敗
        //*********************************************************************
        try {
            authenticator.authenticate("0000000004", "passwor");
            fail("エラーが発生するので、ここは通過しない");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getUserId(), is("0000000004"));
        }

        SystemAccount account2 = VariousDbTestHelper.findById(SystemAccount.class, "0000000004");
        assertThat(account2, notNullValue());
        assertThat("パスワード失敗回数は変更されない", account2.failedCount, is(2));
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


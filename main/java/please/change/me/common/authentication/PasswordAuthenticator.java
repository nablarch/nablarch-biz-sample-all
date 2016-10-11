package please.change.me.common.authentication;

import java.sql.Timestamp;

import nablarch.core.date.BusinessDateProvider;
import nablarch.core.date.SystemTimeProvider;
import nablarch.core.db.connection.AppDbConnection;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.statement.SqlPStatement;
import nablarch.core.db.statement.SqlResultSet;
import nablarch.core.db.statement.SqlRow;
import nablarch.core.db.transaction.SimpleDbTransactionExecutor;
import nablarch.core.db.transaction.SimpleDbTransactionManager;
import nablarch.core.util.StringUtil;


/**
 * データベースに保存されたアカウント情報に対してパスワード認証を行うクラス。<br>
 * <br>
 * PasswordAuthenticatorの特徴を下記に示す。
 * <ul>
 * <li>DBに保存したアカウント情報を使用したパスワード認証ができる。</li>
 * <li>認証時にパスワードの有効期限をチェックできる。</li>
 * <li>連続で指定回数認証に失敗するとユーザIDにロックをかける。(失敗可能回数が指定（0より大きい場合）のみロック機能が有効となる）</li>
 * </ul>
 * PasswordAuthenticatorでは、認証の成功・失敗に関わらず、認証処理においてDBの更新処理が必要なため、内部でトランザクションのコミットを行う。<br>
 * そのため、PasswordAuthenticatorのトランザクション制御が個別アプリケーションの処理に影響を与えないように、
 * 個別アプリケーションとは別のトランザクションを使用するように、PasswordAuthenticatorに{@link SimpleDbTransactionManager}を設定すること。
 *
 * @author Kiyohito Itoh
 */
public class PasswordAuthenticator implements Authenticator {

    /** ユーザIDをロックする認証失敗回数 */
    private int failedCountToLock;

    /** パスワードの暗号化に使用する{@link PasswordEncryptor} */
    private PasswordEncryptor passwordEncryptor;

    /** データベースへのトランザクション制御を行う{@link SimpleDbTransactionManager} */
    private SimpleDbTransactionManager dbManager;

    /** 業務日付を提供する{@link BusinessDateProvider} */
    private BusinessDateProvider businessDateProvider;

    /** システム日時を提供する{@link SystemTimeProvider} */
    private SystemTimeProvider systemTimeProvider;

    /** SQL_IDのプレフィックス */
    private static final String SQLID_PREFIX = "please.change.me.common.authentication.PasswordAuthenticator#";

    /** デフォルトコンストラクタ。 */
    public PasswordAuthenticator() {
        setFailedCountToLock(0);
    }

    /**
     * ユーザIDをロックする認証失敗回数を設定する。
     *
     * @param failedCountToLock ユーザIDをロックする認証失敗回数
     */
    public void setFailedCountToLock(int failedCountToLock) {
        this.failedCountToLock = failedCountToLock;
    }

    /**
     * パスワードの暗号化に使用する{@link PasswordEncryptor}を設定する。
     *
     * @param passwordEncryptor パスワードの暗号化に使用する{@link PasswordEncryptor}
     */
    public void setPasswordEncryptor(PasswordEncryptor passwordEncryptor) {
        this.passwordEncryptor = passwordEncryptor;
    }

    /**
     * データベースへのトランザクション制御を行う{@link SimpleDbTransactionManager}を設定する。
     *
     * @param dbManager データベースへのトランザクション制御を行う{@link SimpleDbTransactionManager}
     */
    public void setDbManager(SimpleDbTransactionManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * 業務日付を提供するクラスのインスタンスを設定する。
     *
     * @param businessDateProvider 業務日付を提供するクラスのインスタンス
     */
    public void setBusinessDateProvider(BusinessDateProvider businessDateProvider) {
        this.businessDateProvider = businessDateProvider;
    }
    
    /**
     * システム日時を提供するクラスのインスタンスを設定する。
     * 
     * @param systemTimeProvider システム日時を提供するクラスのインスタンス
     */
    public void setSystemTimeProvider(SystemTimeProvider systemTimeProvider) {
        this.systemTimeProvider = systemTimeProvider;
    }
    
    /**
     * アカウント情報を使用してユーザを認証する。
     *
     * @param userId ユーザID
     * @param password パスワード
     *
     * @throws AuthenticationFailedException ユーザIDまたはパスワードに一致するユーザが見つからない場合
     * @throws UserIdLockedException ユーザIDがロックされている場合。この例外がスローされる場合は、まだ認証を実施していない。
     * @throws PasswordExpiredException パスワードが有効期限切れの場合。この例外がスローされる場合は、古いパスワードによる認証に成功している。
     */
    public void authenticate(final String userId, final String password)
        throws AuthenticationFailedException, UserIdLockedException, PasswordExpiredException {

        if (userId == null || password == null) {
            throw new AuthenticationFailedException(userId);
        }

        new SimpleDbTransactionExecutor<Void>(dbManager) {
            @Override
            public Void execute(AppDbConnection connection) {
                try {

                    String businessDate = businessDateProvider.getDate();

                    SystemAccount account = findSystemAccount(userId, businessDate);

                    authenticate(account, password, businessDate);

                    dbManager.commitTransaction();
                } catch (AuthenticationFailedException e) {
                    if (isChecksFailedCount()) {
                        // DBで例外が発生した場合には、DB例外を優先する。
                        // 業務エラーとなる認証エラーよりも障害扱いのDB系の例外を優先するべきなので。
                        dbManager.commitTransaction();
                    }
                    throw e;
                }
                return null;
            }
        }
                .doTransaction();
    }

    /**
     * 指定されたユーザIDで、指定された業務日付で有効となっているシステムアカウントを取得する。
     *
     * 取得時には、アカウント情報が変更されないようレコードロックを行う。
     *
     * @param userId ユーザID
     * @param businessDate 業務日付。yyyyMMdd形式
     * @return システムアカウント
     * @throws AuthenticationFailedException ユーザIDまたはパスワードに一致するユーザが見つからない場合
     */
    private SystemAccount findSystemAccount(String userId, String businessDate) throws AuthenticationFailedException {

        if (StringUtil.isNullOrEmpty(userId)) {
            throw new AuthenticationFailedException(userId);
        }

        AppDbConnection connection = DbConnectionContext.getConnection(dbManager.getDbTransactionName());
        SqlPStatement stmt = connection.prepareStatementBySqlId(SQLID_PREFIX + "FIND_SYSTEM_ACCOUNT_AND_LOCK");
        stmt.setString(1, userId);
        stmt.setString(2, businessDate);
        stmt.setString(3, businessDate);

        SqlResultSet resultSet = stmt.retrieve();
        if (resultSet.isEmpty()) {
            throw new AuthenticationFailedException(userId);
        }

        SqlRow systemAccount = resultSet.get(0);
        return new SystemAccount(
                systemAccount.getString("USER_ID"),
                systemAccount.getString("PASSWORD"),
                isUserLocked(systemAccount),
                systemAccount.getString("PASSWORD_EXPIRATION_DATE"),
                systemAccount.getBigDecimal("FAILED_COUNT").intValue());
    }

    /**
     * ユーザがロックされているか否か。
     *
     * @param systemAccount システムアカウント(ユーザ情報)
     * @return 指定されたユーザロックフラグが'1'の場合は、true（ユーザロック中）
     */
    private boolean isUserLocked(SqlRow systemAccount) {
        return "1".equals(systemAccount.getString("USER_ID_LOCKED"));
    }

    /**
     * システムアカウントに対してパスワードによる認証を行う。
     *
     * @param account システムアカウント
     * @param password パスワード
     * @param businessDate 業務日付
     *
     * @throws AuthenticationFailedException ユーザIDまたはパスワードに一致するユーザが見つからない場合
     * @throws UserIdLockedException ユーザIDがロックされている場合。この例外がスローされる場合は、まだ認証を実施していない。
     * @throws PasswordExpiredException パスワードが有効期限切れの場合。この例外がスローされる場合は、古いパスワードによる認証に成功している。
     */
    private void authenticate(SystemAccount account, String password, String businessDate)
            throws AuthenticationFailedException, UserIdLockedException, PasswordExpiredException {

        if (account.isLocked()) {
            throw new UserIdLockedException(account.getUserId(), failedCountToLock);
        }

        String encryptedPassword = passwordEncryptor.encrypt(account.getUserId(), password);

        if (!account.authenticate(encryptedPassword)) {
            if (isChecksFailedCount()) {
                incrementFailedCount(account);
            }
            throw new AuthenticationFailedException(account.getUserId());
        }

        if (account.isPasswordExpired(businessDate)) {
            throw new PasswordExpiredException(
                    account.getUserId(),
                    account.getPasswordExpirationDate(),
                    businessDate);
        }

        if (isChecksFailedCount()) {
            resetFailedCount(account);
        }
        
        updateLastLoginDateTime(account.getUserId());
    }

    /**
     * 認証失敗回数チェックの使用有無を取得する。
     *
     * @return 認証失敗回数チェックを使用する場合はtrue、使用しない場合はfalse
     */
    private boolean isChecksFailedCount() {
        return failedCountToLock > 0;
    }

    /**
     * 認証失敗回数をインクリメントする。<br>
     * 認証失敗回数が指定回数に達した場合は、ユーザIDをロックする。
     *
     * @param account システムアカウント
     */
    private void incrementFailedCount(SystemAccount account) {
        int failedCount = account.getFailedCount() + 1;
        updateFailedCount(account.getUserId(), failedCount, failedCountToLock == failedCount);
    }

    /**
     * 認証失敗回数をリセットする。
     *
     * @param account システムアカウント
     */
    private void resetFailedCount(SystemAccount account) {
        if (account.getFailedCount() == 0) {
            return;
        }
        updateFailedCount(account.getUserId(), 0, false);
    }

    /**
     * 認証失敗回数を更新する。
     *
     * @param userId 更新対象のユーザID
     * @param failedCount 更新で設定する認証失敗回数
     * @param withUserIdLock 一緒にユーザIDをロック状態に更新するか？
     */
    private void updateFailedCount(String userId, int failedCount, boolean withUserIdLock) {
        AppDbConnection conn = DbConnectionContext.getConnection(dbManager.getDbTransactionName());

        SqlPStatement statement;
        if (withUserIdLock) {
            statement = conn.prepareStatementBySqlId(SQLID_PREFIX + "UPDATE_FAILED_COUNT_AND_USER_LOCK");
        } else {
            statement = conn.prepareStatementBySqlId(SQLID_PREFIX + "UPDATE_FAILED_COUNT");
        }
        statement.setInt(1, failedCount);
        statement.setString(2, userId);
        statement.executeUpdate();
    }
    
    /**
     * 最終ログイン日時を更新する。
     * <p/>
     * {@link #getSystemTime()}メソッドから取得したシステム日時を最終ログイン日時の更新に使用する。
     * @param userId 更新対象のユーザID
     */
    private void updateLastLoginDateTime(String userId) {
        AppDbConnection conn = DbConnectionContext.getConnection(dbManager.getDbTransactionName());
        SqlPStatement stmt = conn.prepareStatementBySqlId(SQLID_PREFIX + "UPDATE_LAST_LOGIN_DATETIME");
        stmt.setTimestamp(1, getSystemTime());
        stmt.setString(2, userId);
        stmt.executeUpdate();
    }
    
    /**
     * システム日時を取得する。
     * @return システム日時
     */
    private Timestamp getSystemTime() {
        return systemTimeProvider.getTimestamp();
    }
}


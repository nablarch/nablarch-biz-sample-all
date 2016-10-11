package please.change.me.common.authentication;

/**
 * ユーザのアカウント情報を保持するクラス。
 * 
 * @author Kiyohito Itoh
 */
public class SystemAccount {
    
    /** ユーザID */
    private String userId;

    /** パスワード */
    private String password;
    
    /** ユーザIDがロックされているか否か。 */
    private boolean isUserIdLocked;
    
    /** パスワード有効期限 */
    private String passwordExpirationDate;
    
    /** 認証失敗回数 */
    private int failedCount;
    
    /**
     * コンストラクタ。
     * 
     * @param userId ユーザID
     * @param password パスワード
     * @param isUserIdLocked ユーザがロックされているか否か
     * @param passwordExpirationDate パスワード有効期限
     * @param failedCount 認証失敗回数
     */
    protected SystemAccount(String userId, String password, boolean isUserIdLocked, String passwordExpirationDate, int failedCount) {
        this.userId = userId;
        this.password = password;
        this.isUserIdLocked = isUserIdLocked;
        this.passwordExpirationDate = passwordExpirationDate;
        this.failedCount = failedCount;
    }

    /**
     * ユーザIDを取得する。
     * @return ユーザID
     */
    protected String getUserId() {
        return userId;
    }

    /**
     * ユーザIDがロックされているか否かを判定する。
     * @return ロックされている場合はtrue、ロックされていない場合はfalse
     */
    protected boolean isLocked() {
        return isUserIdLocked;
    }

    /**
     * パスワード有効期限を取得する。
     * @return パスワード有効期限
     */
    protected String getPasswordExpirationDate() {
        return passwordExpirationDate;
    }

    /**
     * 認証失敗回数を取得する。
     * @return 認証失敗回数
     */
    protected int getFailedCount() {
        return failedCount;
    }

    /**
     * パスワードによる認証を行う。
     * @param encryptedPassword 暗号化したパスワード
     * @return 認証成功の場合はtrue、認証失敗の場合はfalse
     */
    protected boolean authenticate(String encryptedPassword) {
        return password.equals(encryptedPassword);
    }

    /**
     * パスワードが有効期限切れか否かを判定する。
     * @param currentDate 現在日付
     * @return 有効期限切れの場合はtrue、有効期限内の場合はfalse
     */
    protected boolean isPasswordExpired(String currentDate) {
        return currentDate.compareTo(passwordExpirationDate) > 0;
    }
}

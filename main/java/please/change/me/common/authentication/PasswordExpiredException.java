package please.change.me.common.authentication;


/**
 * ユーザの認証時にパスワードの有効期限が切れている場合に発生する例外。<br>
 * <br>
 * 対象ユーザのユーザID、パスワード有効期限とチェックに使用した業務日付を保持する。
 * 
 * @author Kiyohito Itoh
 */
public class PasswordExpiredException extends AuthenticationException {
    
    /** ユーザID */
    private String userId;
    
    /** パスワード有効期限 */
    private String passwordExpirationDate;
    
    /** 業務日付 */
    private String businessDate;
    
    /**
     * コンストラクタ。
     * @param userId ユーザID
     * @param passwordExpirationDate パスワードの有効期限
     * @param businessDate 業務日付
     */
    public PasswordExpiredException(String userId, String passwordExpirationDate, String businessDate) {
        this.userId = userId;
        this.businessDate = businessDate;
        this.passwordExpirationDate = passwordExpirationDate;
    }
    
    /**
     * ユーザIDを取得する。
     * @return ユーザID
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * パスワード有効期限を取得する。
     * @return パスワード有効期限
     */
    public String getPasswordExpirationDate() {
        return passwordExpirationDate;
    }
    
    /**
     * 業務日付を取得する。
     * @return 業務日付
     */
    public String getBusinessDate() {
        return businessDate;
    }
}

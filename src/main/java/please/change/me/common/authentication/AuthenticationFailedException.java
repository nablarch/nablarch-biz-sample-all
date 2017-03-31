package please.change.me.common.authentication;


/**
 * アカウント情報の不一致により認証に失敗した場合に発生する例外。<br>
 * <br>
 * 対象ユーザのユーザIDを保持する。
 * 
 * @author Kiyohito Itoh
 */
public class AuthenticationFailedException extends AuthenticationException {
    
    /** ユーザID */
    private String userId;
    
    /**
     * コンストラクタ。
     * @param userId ユーザID
     */
    public AuthenticationFailedException(String userId) {
        this.userId = userId;
    }
    
    /**
     * ユーザIDを取得する。
     * @return ユーザID
     */
    public String getUserId() {
        return userId;
    }
}

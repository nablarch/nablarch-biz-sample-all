package please.change.me.common.authentication;

/**
 * ユーザの認証を行うインタフェース。<br>
 * <br>
 * 認証方式毎に本インタフェースの実装クラスを作成する。
 * 
 * @author Kiyohito Itoh
 */
public interface Authenticator {

    /**
     * アカウント情報を使用してユーザを認証する。<br>
     * <br>
     * 実装クラスでは、認証方式毎にメソッド引数と送出する可能性がある例外を規定すること。
     * 
     * @param userId ユーザID
     * @param password パスワード
     * @throws AuthenticationException 認証例外。
     */
    void authenticate(String userId, String password) throws AuthenticationException;
}

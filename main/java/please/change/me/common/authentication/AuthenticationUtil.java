package please.change.me.common.authentication;

import nablarch.core.repository.SystemRepository;

/**
 * 認証関連の処理のユーティリティクラス。
 *
 * @author Ryo TANAKA
 */
public final class AuthenticationUtil {

    /**
     * パスワード暗号化コンポーネント名。
     */
    private static final String PASSWORD_ENCRYPTOR = "passwordEncryptor";

    /**
     * Authenticatorコンポーネント名。
     */
    private static final String AUTHENTICATOR = "authenticator";

    /**
     * 隠蔽コンストラクタ。
     */
    private AuthenticationUtil() {
    }

    /**
     * パスワードを暗号化する。
     * <p/>
     * パスワードの暗号化に使用する {@link PasswordEncryptor} は、 {@link SystemRepository} から
     * {@value #PASSWORD_ENCRYPTOR} というコンポーネント名で取得する。
     *
     * @param userId ユーザID
     * @param password パスワード（平文）
     * @return 暗号化されたパスワード
     * @see PasswordEncryptor#encrypt(String, String)
     */
    public static String encryptPassword(String userId, String password) {
        PasswordEncryptor passwordEncryptor = SystemRepository.get(PASSWORD_ENCRYPTOR);
        return passwordEncryptor.encrypt(userId, password);
    }

    /**
     * ユーザを認証する。
     * <p/>
     * ユーザの認証に使用する {@link Authenticator} は、 {@link SystemRepository} から
     * {@value #AUTHENTICATOR} というコンポーネント名で取得する。
     *
     * @param userId ユーザID
     * @param password パスワード（平文）
     * @throws AuthenticationFailedException 認証に失敗した場合
     * @throws UserIdLockedException ユーザIDがロックされていた場合
     * @throws PasswordExpiredException パスワードの有効期限が切れていた場合
     * @see Authenticator#authenticate(String, String)
     */
    public static void authenticate(String userId, String password)
            throws AuthenticationFailedException, UserIdLockedException, PasswordExpiredException {
        Authenticator authenticator = SystemRepository.get(AUTHENTICATOR);
        authenticator.authenticate(userId, password);
    }
}

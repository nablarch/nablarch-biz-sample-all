package please.change.me.common.oidc.verification.adb2c.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * IDトークンが有効であるか検証するための機能を提供する。
 */
public interface IdTokenVerifier {

    /**
     * IDトークンが有効であるか検証する。
     *
     * @param idToken IDトークン
     * @return デコード済みのIDトークン
     * @throws JWTVerificationException 無効なIDトークンである場合
     */
    DecodedJWT verify(String idToken) throws JWTVerificationException;
}

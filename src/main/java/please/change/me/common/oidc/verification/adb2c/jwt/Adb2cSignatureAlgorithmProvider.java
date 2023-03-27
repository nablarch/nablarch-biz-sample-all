package please.change.me.common.oidc.verification.adb2c.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.RSAKeyProvider;

/**
 * Azure AD B2Cが発行するトークンの署名検証に使用するアルゴリズムを提供する。
 */
public class Adb2cSignatureAlgorithmProvider implements SignatureAlgorithmProvider {

    /** RSA公開鍵プロバイダ */
    private RSAKeyProvider rsaKeyProvider;

    @Override
    public Algorithm get() {
        return Algorithm.RSA256(rsaKeyProvider);
    }

    /**
     * RSA署名の公開鍵プロバイダを設定する。
     *
     * @param rsaKeyProvider RSA公開鍵プロバイダ
     */
    public void setRsaKeyProvider(RSAKeyProvider rsaKeyProvider) {
        this.rsaKeyProvider = rsaKeyProvider;
    }

}

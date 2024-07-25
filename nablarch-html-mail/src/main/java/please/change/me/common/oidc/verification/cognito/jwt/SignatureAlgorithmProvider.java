package please.change.me.common.oidc.verification.cognito.jwt;

import com.auth0.jwt.algorithms.Algorithm;

/**
 * トークンの署名検証に使用するアルゴリズムを提供する。
 */
public interface SignatureAlgorithmProvider {

    /**
     * 署名検証に使用するアルゴリズムを取得する。
     *
     * @return アルゴリズム
     */
    Algorithm get();
}

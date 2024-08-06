package please.change.me.common.oidc.verification.cognito.jwt;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import nablarch.core.repository.initialization.Initializable;

import java.net.Proxy;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

/**
 * Cognitoが発行するトークンの検証に使用するRSA公開鍵を提供する。
 */
public class CognitoRSAKeyProvider implements RSAKeyProvider, Initializable {

    /** リージョン */
    private String region;

    /** ユーザープール ID */
    private String userPoolId;

    /** JWKプロバイダ */
    private JwkProvider provider;

    @Override
    public RSAPublicKey getPublicKeyById(String keyId) {
        try {
            Jwk jwk = provider.get(keyId);
            return (RSAPublicKey) jwk.getPublicKey();
        } catch (JwkException e) {
            return null;
        }
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        // 公開鍵のみ取得可能であるため、秘密鍵の取得はサポートしない
        throw new UnsupportedOperationException("Get private key is not supported");
    }

    @Override
    public String getPrivateKeyId() {
        // 未定義であるためインタフェースの仕様に則り null を返却する
        return null;
    }

    @Override
    public void initialize() {
        // Cognitoが公開しているJWKSエンドポイントから公開鍵を取得するためのプロバイダを作成する。
        // プロバイダでは以下の設定をすることができる。
        // ・キーID（kidクレームの値）ごとの結果をどれだけの期間いくつまでキャッシュするか
        // ・JWKSエンドポイントへのアクセスをどれだけの期間で何回まで許容するか
        // ・JWKSエンドポイントへのアクセス時にプロキシを使用するか
        // ここでは以下のとおり設定している。
        // ・キーIDは1時間に4つまでキャッシュする（キーのローテーションを跨いだ場合でも通常使用ではキャッシュされる範囲）
        // ・JWKSエンドポイントへのアクセスは1分で10回まで許容する（キャッシュを考慮すると通常使用では到達しない範囲）
        // ・プロキシは使用しない
        this.provider = new JwkProviderBuilder(createUserPoolUrl(region, userPoolId))
                .cached(true)
                .cached(4, 1, TimeUnit.HOURS)
                .rateLimited(true)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .proxied(Proxy.NO_PROXY)
                .build();
    }

    /**
     * ユーザープールのリージョンを設定する。
     *
     * @param region リージョン
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * ユーザープールIDを設定する。
     *
     * @param userPoolId ユーザープールID
     */
    public void setUserPoolId(String userPoolId) {
        this.userPoolId = userPoolId;
    }

    /**
     * ユーザープールのURLを作成する。
     *
     * @param region リージョン
     * @param userPoolId ユーザープールID
     * @return ユーザープールのURL
     */
    private String createUserPoolUrl(String region, String userPoolId) {
        return "https://cognito-idp." + region + ".amazonaws.com/" + userPoolId;
    }
}

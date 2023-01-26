package please.change.me.common.oidc.verification.adb2c.jwt;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import nablarch.core.repository.initialization.Initializable;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

/**
 * Azure AD B2Cが発行するトークンの検証に使用するRSA公開鍵を提供する。
 */
public class Adb2cRSAKeyProvider implements RSAKeyProvider, Initializable {

    /** テナント名 */
    private String tenant;

    /** サインインポリシー名 */
    private String signInPolicy;

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
        // Azure AD B2Cが公開しているJWKSエンドポイントから公開鍵を取得するためのプロバイダを作成する。
        // プロバイダでは以下の設定をすることができる。
        // ・キーID（kidクレームの値）ごとの結果をどれだけの期間いくつまでキャッシュするか
        // ・JWKSエンドポイントへのアクセスをどれだけの期間で何回まで許容するか
        // ・JWKSエンドポイントへのアクセス時にプロキシを使用するか
        // ここでは以下のとおり設定している。
        // ・キーIDは1時間に4つまでキャッシュする（キーのローテーションを跨いだ場合でも通常使用ではキャッシュされる範囲）
        // ・JWKSエンドポイントへのアクセスは1分で10回まで許容する（キャッシュを考慮すると通常使用では到達しない範囲）
        // ・プロキシは使用しない
        this.provider = new JwkProviderBuilder(resolveJwksUrl(tenant, signInPolicy))
                .cached(true)
                .cached(4, 1, TimeUnit.HOURS)
                .rateLimited(true)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .proxied(Proxy.NO_PROXY)
                .build();
    }

    /**
     * テナント名を設定する。
     *
     * @param tenant テナント名
     */
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    /**
     * サインインポリシー名を設定する。
     *
     * @param signInPolicy サインインポリシー名
     */
    public void setSignInPolicy(String signInPolicy) {
        this.signInPolicy = signInPolicy;
    }

    /**
     * JWKS(JSON Web Key Set)のURLを取得する。
     *
     * @param tenant テナント名
     * @param signInPolicy サインインポリシー名
     * @return JWKSのURL
     */
    private URL resolveJwksUrl(String tenant, String signInPolicy) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(createMetaDataUrl(tenant, signInPolicy));
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                String content = EntityUtils.toString(entity);
                OpenIdMetaData openIdMetaData = new ObjectMapper().readValue(content, OpenIdMetaData.class);

                EntityUtils.consume(entity);
                return URI.create(openIdMetaData.getJwksUri()).toURL();
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * メタデータエンドポイントのURLを作成する。
     *
     * @param tenant テナント名
     * @param signInPolicy サインインポリシー名
     * @return メタデータエンドポイントのURL
     */
    private String createMetaDataUrl(String tenant, String signInPolicy) {
        return "https://" + tenant + ".b2clogin.com/" + tenant + ".onmicrosoft.com/" + signInPolicy + "/v2.0/.well-known/openid-configuration";
    }
}

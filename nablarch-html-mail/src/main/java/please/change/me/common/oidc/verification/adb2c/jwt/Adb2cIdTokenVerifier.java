package please.change.me.common.oidc.verification.adb2c.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * Azure AD B2Cが発行するIDトークンが有効であるか検証するための機能を提供する。
 */
public class Adb2cIdTokenVerifier implements IdTokenVerifier {

    /** テナント名 */
    private String tenant;

    /** アプリケーションID */
    private String applicationId;

    /** ディレクトリID */
    private String directoryId;

    /** 署名アルゴリズムプロバイダ */
    private SignatureAlgorithmProvider signatureAlgorithmProvider;

    @Override
    public DecodedJWT verify(String idToken) throws JWTVerificationException {
        // トークンが有効であるか検証する方法はAzure AD B2Cのガイドに従う。
        //   https://learn.microsoft.com/ja-jp/azure/active-directory-b2c/tokens-overview
        // クライアント側でIDトークン取得後に即時送信されることを想定し、有効期限の許容範囲は60秒とする。
        JWTVerifier verifier = JWT.require(signatureAlgorithmProvider.get())
                .acceptExpiresAt(60)
                .withAudience(applicationId)
                .withIssuer(createIssuer(tenant, directoryId))
                .build();
        return verifier.verify(idToken);
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
     * アプリケーションIDを設定する。
     *
     * @param applicationId アプリケーションID
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * ディレクトリIDを設定する。
     *
     * @param directoryId ディレクトリIDを設定する。
     */
    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }

    /**
     * トークンの署名検証に使用するアルゴリズムプロバイダを設定する。
     *
     * @param signatureAlgorithmProvider 署名アルゴリズムプロバイダ。
     */
    public void setSignatureAlgorithmProvider(SignatureAlgorithmProvider signatureAlgorithmProvider) {
        this.signatureAlgorithmProvider = signatureAlgorithmProvider;
    }

    /**
     * 発行者クレームの値を作成する。
     *
     * @param tenant テナント名
     * @param directoryId ディレクトリID
     * @return 発行者クレームの値
     */
    private String createIssuer(String tenant, String directoryId) {
        return "https://" + tenant + ".b2clogin.com/" + directoryId + "/v2.0/";
    }
}

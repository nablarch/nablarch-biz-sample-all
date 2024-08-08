package please.change.me.common.oidc.verification.cognito.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * Cognitoが発行するIDトークンが有効であるか検証するための機能を提供する。
 */
public class CognitoIdTokenVerifier implements IdTokenVerifier {

    /** リージョン */
    private String region;

    /** ユーザープール ID */
    private String userPoolId;

    /** クライアントID */
    private String clientId;

    /** 署名アルゴリズムプロバイダ */
    private SignatureAlgorithmProvider signatureAlgorithmProvider;

    @Override
    public DecodedJWT verify(String idToken) throws JWTVerificationException {
        // トークンが有効であるか検証する検証方法はCognitoのガイドに従う。
        //   https://docs.aws.amazon.com/ja_jp/cognito/latest/developerguide/amazon-cognito-user-pools-using-tokens-verifying-a-jwt.html
        // クライアント側でIDトークン取得後に即時送信されることを想定し、有効期限の許容範囲は60秒とする。
        JWTVerifier verifier = JWT.require(signatureAlgorithmProvider.get())
                .acceptExpiresAt(60)
                .withAudience(clientId)
                .withIssuer(createUserPoolUrl(region, userPoolId))
                .withClaim("token_use", "id")
                .build();
        return verifier.verify(idToken);
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
     * クライアントIDを設定する。
     *
     * @param clientId クライアントID
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
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

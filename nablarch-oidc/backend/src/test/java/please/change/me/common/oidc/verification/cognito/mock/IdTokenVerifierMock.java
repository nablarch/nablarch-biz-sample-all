package please.change.me.common.oidc.verification.cognito.mock;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import please.change.me.common.oidc.verification.cognito.jwt.IdTokenVerifier;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Objects;

public class IdTokenVerifierMock implements IdTokenVerifier {

    /**
     * IDトークンの指定値に関わらず、固定値が設定されたIDトークンをデコードして返却する。
     * <p>
     * 返却されるトークンの値は以下のとおりである。
     * <ul>
     * <li>subトークンは"hoge"</li>
     * <li>expクレームはメソッド呼び出し時点から1時間後の日時</li>
     * <li>audクレームは"clientId"</li>
     * <li>issクレームは"https://cognito-idp.region.amazonaws.com/userPoolId"</li>
     * <li>token_useクレームは"id"</li>
     * <li>algクレームは"none"</li>
     * <li>署名は無し</li>
     * </ul>
     * なお、検証が失敗することをテストするために、IDトークンに"fail"を指定した場合は検証を失敗させる。
     *
     * @param idToken IDトークン
     * @return デコード済みIDトークン
     * @throws JWTVerificationException idTokenに"fail"を指定した場合
     */
    @Override
    public DecodedJWT verify(String idToken) throws JWTVerificationException {
        if (Objects.equals(idToken, "fail")) {
            throw new JWTVerificationException("fail");
        }

        String fixedToken = JWT.create()
                .withSubject("hoge")
                .withExpiresAt(ZonedDateTime.now(ZoneId.systemDefault()).plusHours(1).toInstant())
                .withAudience("clientId")
                .withIssuer("https://cognito-idp.region.amazonaws.com/userPoolId")
                .withClaim("token_use", "id")
                .withHeader(new HashMap<>() {{
                    put("alg", Algorithm.none().getName());
                }})
                .sign(Algorithm.none());

        JWTVerifier verifier = JWT.require(Algorithm.none()).build();

        return verifier.verify(fixedToken);
    }
}

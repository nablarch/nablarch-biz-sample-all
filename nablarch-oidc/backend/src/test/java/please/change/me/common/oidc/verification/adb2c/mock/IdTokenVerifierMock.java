package please.change.me.common.oidc.verification.adb2c.mock;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import please.change.me.common.oidc.verification.adb2c.jwt.IdTokenVerifier;

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
     * <li>audクレームは"applicationId"</li>
     * <li>issクレームは"https://tenant.b2clogin.com/directoryId/v2.0/"</li>
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
                .withAudience("applicationId")
                .withIssuer("https://tenant.b2clogin.com/directoryId/v2.0/")
                .withHeader(new HashMap<>() {{
                    put("alg", Algorithm.none().getName());
                }})
                .sign(Algorithm.none());

        JWTVerifier verifier = JWT.require(Algorithm.none()).build();

        return verifier.verify(fixedToken);
    }
}

package please.change.me.common.oidc.verification.cognito.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link CognitoIdTokenVerifier}のテスト。
 */
class CognitoIdTokenVerifierTest {

    private CognitoIdTokenVerifier sut;

    @BeforeEach
    void setUp() {
        sut = new CognitoIdTokenVerifier();
        sut.setRegion("region");
        sut.setUserPoolId("userPoolId");
        sut.setClientId("clientId");
        sut.setSignatureAlgorithmProvider(Algorithm::none);
    }

    @Test
    void IDトークンのaudienceが一致しなければ検証に失敗する() {
        String idToken = JWT.create()
                .withSubject("hoge")
                .withExpiresAt(ZonedDateTime.now(ZoneId.systemDefault()).toInstant())
                .withAudience("dummy")
                .withIssuer("https://cognito-idp.region.amazonaws.com/userPoolId")
                .withClaim("token_use", "id")
                .withHeader(new HashMap<>() {{
                    put("alg", Algorithm.none().getName());
                }})
                .sign(Algorithm.none());

        assertThrows(JWTVerificationException.class, () -> sut.verify(idToken));
    }

    @Test
    void IDトークンのissuerが一致しなければ検証に失敗する() {
        String idToken = JWT.create()
                .withSubject("hoge")
                .withExpiresAt(ZonedDateTime.now(ZoneId.systemDefault()).toInstant())
                .withAudience("clientId")
                .withIssuer("dummy")
                .withClaim("token_use", "id")
                .withHeader(new HashMap<>() {{
                    put("alg", Algorithm.none().getName());
                }})
                .sign(Algorithm.none());

        assertThrows(JWTVerificationException.class, () -> sut.verify(idToken));
    }

    @Test
    void IDトークンの有効期限を過ぎていれば検証に失敗する() {
        String idToken = JWT.create()
                .withSubject("hoge")
                .withExpiresAt(ZonedDateTime.now(ZoneId.systemDefault()).minusMinutes(5).toInstant())
                .withAudience("clientId")
                .withIssuer("https://cognito-idp.region.amazonaws.com/userPoolId")
                .withClaim("token_use", "id")
                .withHeader(new HashMap<>() {{
                    put("alg", Algorithm.none().getName());
                }})
                .sign(Algorithm.none());

        assertThrows(JWTVerificationException.class, () -> sut.verify(idToken));
    }

    @Test
    void IDトークンのtoken_useが一致しなければ検証に失敗する() {
        String idToken = JWT.create()
                .withSubject("hoge")
                .withExpiresAt(ZonedDateTime.now(ZoneId.systemDefault()).toInstant())
                .withAudience("clientId")
                .withIssuer("https://cognito-idp.region.amazonaws.com/userPoolId")
                .withClaim("token_use", "dummy")
                .withHeader(new HashMap<>() {{
                    put("alg", Algorithm.none().getName());
                }})
                .sign(Algorithm.none());

        assertThrows(JWTVerificationException.class, () -> sut.verify(idToken));
    }

    @Test
    void IDトークンが有効であれば検証が成功する() {
        String idToken = JWT.create()
                .withSubject("hoge")
                .withExpiresAt(ZonedDateTime.now(ZoneId.systemDefault()).toInstant())
                .withAudience("clientId")
                .withIssuer("https://cognito-idp.region.amazonaws.com/userPoolId")
                .withClaim("token_use", "id")
                .withHeader(new HashMap<>() {{
                    put("alg", Algorithm.none().getName());
                }})
                .sign(Algorithm.none());

        assertDoesNotThrow(() -> sut.verify(idToken));
    }


}

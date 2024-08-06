package please.change.me.common.oidc.verification.adb2c.jwt;

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
 * {@link Adb2cIdTokenVerifier}のテスト。
 */
class Adb2cIdTokenVerifierTest {

    private Adb2cIdTokenVerifier sut;

    @BeforeEach
    void setUp() {
        sut = new Adb2cIdTokenVerifier();
        sut.setTenant("tenant");
        sut.setDirectoryId("directoryId");
        sut.setApplicationId("applicationId");
        sut.setSignatureAlgorithmProvider(Algorithm::none);
    }

    @Test
    void IDトークンのaudienceが一致しなければ検証に失敗する() {
        String idToken = JWT.create()
                .withSubject("hoge")
                .withExpiresAt(ZonedDateTime.now(ZoneId.systemDefault()).toInstant())
                .withAudience("dummy")
                .withIssuer("https://tenant.b2clogin.com/directoryId/v2.0/")
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
                .withAudience("applicationId")
                .withIssuer("dummy")
                .withHeader(new HashMap<>() {{
                    put("alg", Algorithm.none().getName());
                }})
                .sign(Algorithm.none());

        assertThrows(JWTVerificationException.class, () -> sut.verify(idToken));
    }

    @Test
    void IDトークンの有効期限を過ぎていれば401エラーが返却される() {
        String idToken = JWT.create()
                .withSubject("hoge")
                .withExpiresAt(ZonedDateTime.now(ZoneId.systemDefault()).minusMinutes(5).toInstant())
                .withAudience("applicationId")
                .withIssuer("https://tenant.b2clogin.com/directoryId/v2.0/")
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
                .withAudience("applicationId")
                .withIssuer("https://tenant.b2clogin.com/directoryId/v2.0/")
                .withHeader(new HashMap<>() {{
                    put("alg", Algorithm.none().getName());
                }})
                .sign(Algorithm.none());

        assertDoesNotThrow(() -> sut.verify(idToken));
    }
}

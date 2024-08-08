package please.change.me.common.oidc.verification.adb2c.action;

import nablarch.common.web.session.SessionUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpResponse;
import nablarch.test.core.http.SimpleRestTestSupport;
import nablarch.test.junit5.extension.http.SimpleRestTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

@SimpleRestTest
class LoginActionTest {

    SimpleRestTestSupport support;

    @Test
    void IDトークンが送信されていなければ400エラーが返却される() {
        HttpResponse response = support.sendRequest(support
                .post("/adb2c/login")
                .setContentType("application/json"));

        support.assertStatusCode("ログイン", HttpResponse.Status.BAD_REQUEST, response);
    }

    @Test
    void IDトークンの検証に失敗した場合は401エラーが返却される() {
        HttpResponse response = support.sendRequest(support
                .post("/adb2c/login")
                .setBody(new HashMap<String, Object>(){{
                    put("idToken", "fail");
                }}));

        support.assertStatusCode("ログイン", HttpResponse.Status.UNAUTHORIZED, response);
    }

    @Test
    void IDトークンの検証が成功すれば認証に成功する() {
        ExecutionContext context = new ExecutionContext();

        HttpResponse response = support.sendRequestWithContext(support
                .post("/adb2c/login")
                .setBody(new HashMap<String, Object>(){{
                    put("idToken", "success");
                }}),
                context);

        support.assertStatusCode("ログイン", HttpResponse.Status.NO_CONTENT, response);
        Assertions.assertEquals("hoge", SessionUtil.get(context, "user.id"));
    }
}

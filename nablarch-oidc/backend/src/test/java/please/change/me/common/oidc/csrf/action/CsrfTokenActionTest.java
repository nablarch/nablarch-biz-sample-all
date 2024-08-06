package please.change.me.common.oidc.csrf.action;

import com.jayway.jsonpath.JsonPath;
import nablarch.common.web.WebConfig;
import nablarch.common.web.WebConfigFinder;
import nablarch.common.web.session.SessionUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpResponse;
import nablarch.test.core.http.SimpleRestTestSupport;
import nablarch.test.junit5.extension.http.SimpleRestTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SimpleRestTest
class CsrfTokenActionTest {

    SimpleRestTestSupport support;

    @Test
    void CSRFトークンの値とHTTPヘッダー名が取得できる() {
        ExecutionContext context = new ExecutionContext();
        WebConfig webConfig = WebConfigFinder.getWebConfig();
        SessionUtil.put(context, webConfig.getCsrfTokenSessionStoredVarName(), "test");

        HttpResponse response = support.sendRequestWithContext(support.get("/csrf_token"), context);

        support.assertStatusCode("CSRFトークン取得", HttpResponse.Status.OK, response);

        String json = response.getBodyString();
        assertEquals("X-CSRF-TOKEN", JsonPath.read(json, "$.csrfTokenHeaderName"));
        assertEquals("test", JsonPath.read(json, "$.csrfTokenValue"));
    }
}

package please.change.me.simulator.incoming.http.action;

import nablarch.fw.ExecutionContext;
import nablarch.fw.results.BadRequest;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.MockHttpRequest;
import nablarch.test.RepositoryInitializer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import please.change.me.simulator.common.SendSyncSupportWrapperConcurrent;
import please.change.me.simulator.incoming.http.launcher.HttpIncomingSimulatorLauncher;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link RequestIdAwareHttpIncomingSimulateAction}のテスト。
 * 
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class RequestIdAwareHttpIncomingSimulateActionTest {
    /** テスト対象 */
    RequestIdAwareHttpIncomingSimulateAction target = new RequestIdAwareHttpIncomingSimulateAction();

    @BeforeClass
    public static void initialize() {
        RepositoryInitializer.initializeDefaultRepository();
        HttpIncomingSimulatorLauncher.initializeRepository();
    }

    /** Excelの読み出し箇所をリセットする。 */
    @After
    public void reset() {
        SendSyncSupportWrapperConcurrent.getInstance().reset();
    }

    @AfterClass
    public static void terminate() {
    	RepositoryInitializer.initializeDefaultRepository();
    }

    /** HTTPリクエストからリクエストIDを抽出できること。*/
    @Test
    public void testGetRequestId() {
        HttpRequest req = new MockHttpRequest().setParam("requestId", "RM11AC0201");
        String requestId = target.getRequestId(req);
        assertThat(requestId, is("RM11AC0201"));
    }

    /** HTTPリクエストの内容によってレスポンスを変えられること（RM11AC0201）*/
    @Test
    public void testHandleRM11AC0201() {
        HttpRequest req = new MockHttpRequest().setParam("requestId", "RM11AC0201");
        HttpResponse res = target.handle(req, new ExecutionContext());
        String bodyString = res.getBodyString();
        assertThat(bodyString,
                   is("{\"failureCode\":\"\",\"userInfoId\":\"あ\",\"dataKbn\":\"0\"}"));
    }

    /** HTTPリクエストの内容によってレスポンスを変えられること（RM11AC0202）*/
    @Test
    public void testHandleRM11AC0202() {
        HttpRequest req = new MockHttpRequest().setParam("requestId", "RM11AC0202");
        HttpResponse res = target.handle(req, new ExecutionContext());

        String bodyString = res.getBodyString();
        assertThat(bodyString,
                   is("<?xml version=\"1.0\" encoding=\"Shift-JIS\" " +
                              "?><response><failureCode></failureCode><userInfoId>あ</userInfoId><dataKbn>0</dataKbn" +
                              "></response>"));
    }

    @Test(expected = BadRequest.class)
    public void testRequestIdNull() {
        HttpRequest req = new MockHttpRequest().setParam("requestId", (String[]) null);
        target.handle(req, new ExecutionContext());
    }


    @Test(expected = BadRequest.class)
    public void testTooManyRequestId() {
        HttpRequest req = new MockHttpRequest().setParam("requestId");
        target.handle(req, new ExecutionContext());
    }

}
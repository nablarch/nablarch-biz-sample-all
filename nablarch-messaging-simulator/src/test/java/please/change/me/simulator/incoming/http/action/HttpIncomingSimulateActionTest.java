package please.change.me.simulator.incoming.http.action;

import nablarch.fw.web.MockHttpRequest;
import nablarch.test.RepositoryInitializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import please.change.me.simulator.incoming.http.launcher.HttpIncomingSimulatorLauncher;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link HttpIncomingSimulateAction}のテスト。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class HttpIncomingSimulateActionTest {

    @BeforeClass
    public static void initialize() {
        RepositoryInitializer.initializeDefaultRepository();
        HttpIncomingSimulatorLauncher.initializeRepository();
    }
    @AfterClass
    public static void terminate() {
        RepositoryInitializer.initializeDefaultRepository();
    }

    @Test
    public void testGetRequestId() {
        HttpIncomingSimulateAction target = new HttpIncomingSimulateAction();
        String requestId = target.getRequestId(new MockHttpRequest());
        assertThat(requestId, is("RM11AC0201"));
    }
}

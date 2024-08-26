package please.change.me.simulator.incoming.mom.action;

import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.fw.ExecutionContext;
import nablarch.fw.messaging.MessagingContext;
import nablarch.fw.messaging.ReceivedMessage;
import nablarch.fw.messaging.SendingMessage;
import nablarch.test.RepositoryInitializer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import please.change.me.simulator.common.DummyMessagingContext;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

/**
 * {@link MomIncomingSimulateAction}のテスト。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class MomIncomingSimulateActionTest {

    private static final String REQUEST_ID = "RM11AC0302";
    private MomIncomingSimulateAction target = new MomIncomingSimulateAction();

    @Before
    public void setUp() {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader("incoming-mom-simulator-component-configuration.xml");
        SystemRepository.load(new DiContainer(loader));

        SystemRepository.load(new DiContainer(new XmlComponentDefinitionLoader("unit-test.xml")));
        SystemRepository.load(() -> {
            Map<String, Object> m = new HashMap<>();
            m.put("request-id", REQUEST_ID);
            return m;
        });
    }

    @After
    public void tearDown() {
    	RepositoryInitializer.initializeDefaultRepository();
    }

    @Test
    public void testHandle() {
        ReceivedMessage msg = new ReceivedMessage(new byte[0]);
        msg.setHeader("ReplyTo", "ReplyToQueue");
        msg.setMessageId(REQUEST_ID);


        DummyMessagingContext ctx = new DummyMessagingContext();
        MessagingContext.attach(ctx);

        target.handle(msg, new ExecutionContext());
        {
            SendingMessage result = ctx.getSendingMessage();
            assertThat(result, is(not(nullValue())));
            String res = encode(result.getBodyBytes());
            assertThat(res, startsWith("202"));
        }

        target.handle(msg, new ExecutionContext());
        {
            SendingMessage result = ctx.getSendingMessage();
            assertThat(result, is(not(nullValue())));
            String res = encode(result.getBodyBytes());
            assertThat(res, startsWith("200"));
        }
    }

    private String encode(byte[] b) {
        return new String(b, Charset.forName("Shift-JIS"));
    }

}
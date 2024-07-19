package please.change.me.simulator.outgoing.action;

import nablarch.core.dataformat.DataRecord;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.fw.DataReader;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Result;
import nablarch.fw.messaging.MessagingContext;
import nablarch.fw.messaging.SendingMessage;
import nablarch.test.RepositoryInitializer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import please.change.me.simulator.common.DummyMessagingContext;
import please.change.me.simulator.common.SendSyncSupportWrapperConcurrent;
import please.change.me.simulator.outgoing.CyclicDataReader;

import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * {@link OutgoingSimulatorAction}のテスト。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class OutgoingSimulatorActionTest {
    private OutgoingSimulatorAction target = new OutgoingSimulatorAction();


    @Before
    public void setUp() {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader("outgoing-simulator-component-configuration.xml");

        SystemRepository.load(new DiContainer(loader));

        SystemRepository.load(new DiContainer(new XmlComponentDefinitionLoader("unit-test.xml")));
        System.setProperty("requests-to-send", "mom-RequestsToSend-104.csv");
        SendSyncSupportWrapperConcurrent.getInstance().reset();
    }

    @After
    public void tearDown() {
    	RepositoryInitializer.initializeDefaultRepository();
    }


    @Test
    public void testAsync() {
        DataRecord record = new DataRecord();
        record.put("synchronous", "false");
        record.put("requestId", "RM11AC0305");
        DummyMessagingContext ctx = new DummyMessagingContext();
        MessagingContext.attach(ctx);
        Result result = target.doData(record, new ExecutionContext());
        assertThat(result.isSuccess(), is(true));
        SendingMessage sendingMessage = ctx.getSendingMessage();
        String s = new String(sendingMessage.getBodyBytes(), Charset.forName("Shift-JIS"));
        assertThat(s, containsString("あいうえお"));
    }


    @Test
    public void testCreateReader() {
        ExecutionContext ctx = new ExecutionContext();
        ctx.setSessionScopedVar(OutgoingSimulatorAction.SEND_COUNT_OPTION, "1");
        DataReader<DataRecord> reader = target.createReader(ctx);
        assertThat(reader.getClass().getName(), is(CyclicDataReader.class.getName()));
    }


    @Test
    public void testSendCountNull() {
        ExecutionContext ctx = new ExecutionContext();
        ctx.setSessionScopedVar(OutgoingSimulatorAction.SEND_COUNT_OPTION, null);

        assertThat(target.getSendCountOrElse(ctx, 100), is(100));

    }

    @Test
    public void testSendCountOne() {
        ExecutionContext ctx = new ExecutionContext();
        ctx.setSessionScopedVar(OutgoingSimulatorAction.SEND_COUNT_OPTION, "1");

        assertThat(target.getSendCountOrElse(ctx, 0), is(1));
    }

    @Test
    public void testSendCountTwo() {
        ExecutionContext ctx = new ExecutionContext();
        ctx.setSessionScopedVar(OutgoingSimulatorAction.SEND_COUNT_OPTION, "2");

        assertThat(target.getSendCountOrElse(ctx, 0), is(2));
    }


}

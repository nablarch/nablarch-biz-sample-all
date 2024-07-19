package please.change.me.simulator.incoming.http.launcher;

import nablarch.test.NablarchTestUtils;
import nablarch.test.RepositoryInitializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

/**
 * {@link HttpIncomingSimulatorLauncher}のテスト。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class HttpIncomingSimulatorLauncherTest {

    /** コンストラクタの起動テスト */
    @Test
    public void testConstructor() {
        NablarchTestUtils.invokePrivateDefaultConstructor(HttpIncomingSimulatorLauncher.class);
    }

    private static final int HTTP_SERVER_PORT = 17373;

    private static Properties systemPropOrig;

    @BeforeClass
    public static void saveSystemProperties() {
        systemPropOrig = System.getProperties();
    }

    @AfterClass
    public static void revertEnv() {
        RepositoryInitializer.revertDefaultRepository();
        System.setProperties(systemPropOrig);
    }

    /** 指定された構成でシミュレータが起動できること。*/
    @Test
    public void testStartServer() {
        System.setProperty("port", String.valueOf(HTTP_SERVER_PORT));
        HttpIncomingSimulatorLauncher.main();
    }
}
package please.change.me;

import jakarta.jms.Queue;
import nablarch.core.repository.initialization.Initializable;
import nablarch.fw.messaging.provider.JmsMessagingProvider;
import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.core.config.CoreAddressConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnector;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ActiveMQを組み込みモードで起動して使用するMessagingProvider。
 * <p>
 * メッセージングの受信側に組み込むことを想定している。<br />
 * 送信側は、本MessagingProviderで起動したActiveMQに接続する。
 * </p>
 * @author Nabu Rakutaro
 */
@SuppressWarnings({"blacklistJavaApi", "ProhibitedExceptionThrown"})
public class EmbeddedMessagingProvider extends JmsMessagingProvider implements Initializable {

    /** キューマネージャ */
    private EmbeddedActiveMQ embedded;

    /** キューのリスト(親クラスから簡単に取り出す方法がないため、本クラスでも保持する) */
    private final List<ActiveMQQueue> queueList = new ArrayList<>();

    /** アプリケーションが接続するためのURI */
    private String url = "tcp://localhost:61616";

    /**
     * 初期化処理を行う。
     *
     * <p>組み込みActiveMQの起動と、ConnectionFactoryの設定を行う。
     * 本処理で使用するパラメータは、コンポーネント定義ファイルで設定する前提である。
     */
    @Override
    public void initialize() {
        ConfigurationImpl config = new ConfigurationImpl();
        try {
            config.addAcceptorConfiguration("tcp", url);
        } catch (Exception e) {
            // addAcceptorConfigurationがExceptionを送出する可能性があるため、Exceptionを指定してcatchしている
            throw new RuntimeException("an Error occurred while launch the EmbeddedActiveMQ", e);
        }
        config.setSecurityEnabled(false);
        config.setPersistenceEnabled(false);
        
        List<QueueConfiguration> queueConfigs = queueList.stream()
                .map(ActiveMQQueue::getQueueName)
                .map(name -> {
                    QueueConfiguration queueConfig = new QueueConfiguration(name);
                    queueConfig.setRoutingType(RoutingType.ANYCAST);
                    return queueConfig;
                })
                .toList();
        config.setQueueConfigs(queueConfigs);
        CoreAddressConfiguration addressConfig = new CoreAddressConfiguration();
        addressConfig.setName("address.nablarch-test");
        addressConfig.setQueueConfigs(queueConfigs);
        addressConfig.addRoutingType(RoutingType.ANYCAST);

        embedded = new EmbeddedActiveMQ();
        embedded.setConfiguration(config);
        startServer();

        try {
            setConnectionFactory(ActiveMQJMSClient.createConnectionFactory(url, "nablarch-test"));
        } catch (Exception e) {
            // createConnectionFactoryがExceptionを送出する可能性があるため、Exceptionを指定してcatchしている
            throw new RuntimeException(e);
        }
    }

    /**
     * 内蔵サーバを開始する。
     */
    protected void startServer() {
        try {
            embedded.start();
        } catch (Exception e) {
            // startがExceptionを送出する可能性があるため、Exceptionを指定してcatchしている
            stopServer();
            throw new RuntimeException(e);
        }
    }

    /**
     * 内蔵サーバを停止する。
     */
    protected void stopServer() {
        try {
            embedded.stop();
            // このメソッドを呼ばないと、非デーモンスレッドのスレッドプールが残り続けてVMの停止が非常に遅くなる
            InVMConnector.resetThreadPool();
            embedded = null;
        } catch (Exception e) {
            // stopがExceptionを送出する可能性があるため、Exceptionを指定してcatchしている
            throw new RuntimeException(e);
        }
    }

    /**
     * このキューマネージャが管理するキューの論理名を設定する。
     *
     * @param names キュー名の一覧
     */
    public void setQueueNames(List<String> names) {
        Map<String, Queue> queueMap = new HashMap<>();
        names.forEach(name -> {
            ActiveMQQueue activeMQQueue = new ActiveMQQueue(name);
            queueList.add(activeMQQueue);
            queueMap.put(name, activeMQQueue);
        });
        setDestinations(queueMap);
    }

    /**
     * アプリケーションが接続に使用するURIを指定する。
     *
     * @param url アプリケーションが接続に使用するURI
     */
    public void setUrl(String url) {
        this.url = url;
    }
}

package please.change.me.messaging;

import nablarch.fw.messaging.FwHeader;
import nablarch.fw.messaging.FwHeaderDefinition;
import nablarch.fw.messaging.ReceivedMessage;
import nablarch.fw.messaging.RequestMessage;
import nablarch.fw.messaging.SendingMessage;

/**
 * 何もしないフレームワーク制御ヘッダ定義クラス。<br>
 * <p>
 * フレームワーク制御ヘッダを解析させないために使用する。
 * </p>
 * @author Masaya Seko
 * @since 1.4.2
 */
public class DummyFwHeaderDefinition implements FwHeaderDefinition {

    @Override
    public RequestMessage readFwHeaderFrom(ReceivedMessage message) {
        //使用しない
        return null;
    }

    @Override
    public void writeFwHeaderTo(SendingMessage message, FwHeader header) {
        //何もしない
    }

}

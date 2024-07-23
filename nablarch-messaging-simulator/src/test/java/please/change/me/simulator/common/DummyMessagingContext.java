package please.change.me.simulator.common;

import nablarch.fw.messaging.MessagingContext;
import nablarch.fw.messaging.ReceivedMessage;
import nablarch.fw.messaging.SendingMessage;

/**
 * ダミーのMessagingContextクラス。
 *
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class DummyMessagingContext extends MessagingContext {

    private SendingMessage sendingMessage;

    public SendingMessage getSendingMessage() {
        return sendingMessage;
    }
    @Override
    public String sendMessage(SendingMessage message) {
        this.sendingMessage = message;
        return null;
    }

    @Override
    public ReceivedMessage receiveMessage(String receiveQueue, String messageId, long timeout) {
        return null;
    }

    @Override
    public void close() {

    }
}

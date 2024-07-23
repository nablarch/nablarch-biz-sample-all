package please.change.me.simulator.incoming.mom.action;

import nablarch.core.repository.SystemRepository;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.messaging.MessagingContext;
import nablarch.fw.messaging.ReceivedMessage;
import nablarch.fw.messaging.ResponseMessage;
import please.change.me.simulator.common.MessageReadSupport;

/**
 * MOM同期メッセージ受信シミュレートアクション。
 *
 * @author Ryo TANAKA
 * @since 1.4.2
 */
public class MomIncomingSimulateAction implements Handler<ReceivedMessage, ResponseMessage> {

    /** サポートクラス */
    private final MessageReadSupport messageReadSupport = new MessageReadSupport();

    /** {@inheritDoc} */
    @Override
    public ResponseMessage handle(ReceivedMessage message, ExecutionContext context) {
        String id = getRequestId(message);
        ResponseMessage reply = messageReadSupport.getMessageForMom(id, message);
        MessagingContext.getInstance().send(reply);
        return null;
    }

    /**
     * リクエストID(読み込むExcelファイル)を返す。<br>
     * @param message 受信したメッセージ
     * @return リクエストID
     */
    protected String getRequestId(ReceivedMessage message) {
        return SystemRepository.getString("request-id");
    }

}

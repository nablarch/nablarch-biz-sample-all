package please.change.me;

import nablarch.core.beans.BeanUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.messaging.RequestMessage;
import nablarch.fw.messaging.ResponseMessage;
import nablarch.fw.messaging.action.MessagingAction;
import please.change.me.form.ResponseForm;

/**
 * 受信した電文をFormUrlEncoded形式の応答電文で返す。
 */
public class SendFormUrlEncodedAction extends MessagingAction {

    /**
     * 受信した電文をFormUrlEncoded形式の応答電文で返す。
     *
     * @param request 要求電文オブジェクト
     * @param context 実行コンテキスト
     * @return 応答電文オブジェクト
     */
    @Override
    protected ResponseMessage onReceive(RequestMessage request, ExecutionContext context) {
        // 応答データ返却
        ResponseForm resForm = BeanUtil.createAndCopy(ResponseForm.class, request.getParamMap());
        return request.reply().addRecord(resForm);
    }

}

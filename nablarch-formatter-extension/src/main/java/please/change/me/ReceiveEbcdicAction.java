package please.change.me;

import nablarch.core.beans.BeanUtil;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.fw.ExecutionContext;
import nablarch.fw.messaging.RequestMessage;
import nablarch.fw.messaging.ResponseMessage;
import nablarch.fw.messaging.action.MessagingAction;
import please.change.me.form.RequestForm;
import please.change.me.form.ResponseForm;

/**
 * EBCDIC(CP930)のダブルバイト文字列（シフトコード付き）の電文を受信して、ログを出力する。
 */
public class ReceiveEbcdicAction extends MessagingAction {

    /**
     * ロガー。
     */
    private static final Logger LOGGER = LoggerManager.get(ReceiveEbcdicAction.class);

    /**
     * 受信した電文をログに出力する。
     *
     * @param request 要求電文オブジェクト
     * @param context 実行コンテキスト
     * @return 応答電文オブジェクト
     */
    @Override
    protected ResponseMessage onReceive(RequestMessage request,
            ExecutionContext context) {
        // 受信データをログに出力
        RequestForm form = BeanUtil.createAndCopy(RequestForm.class, request.getParamMap());
        LOGGER.logInfo(String.format("項目1:%s", form.getKey1()));
        LOGGER.logInfo(String.format("項目2:%s", form.getKey2()));
        // 応答データ返却
        ResponseForm resForm = BeanUtil.createAndCopy(ResponseForm.class, form);
        return request.reply().addRecord(resForm);
    }

}

package please.change.me.simulator.outgoing.action;

import nablarch.core.dataformat.DataRecord;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.repository.SystemRepository;
import nablarch.fw.DataReader;
import nablarch.fw.DataReaderFactory;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Result;
import nablarch.fw.Result.Success;
import nablarch.fw.action.FileBatchActionBase;
import nablarch.fw.messaging.MessageSender;
import nablarch.fw.messaging.MessagingContext;
import nablarch.fw.messaging.SendingMessage;
import nablarch.fw.messaging.SyncMessage;
import nablarch.fw.reader.FileDataReader;
import please.change.me.simulator.common.MessageReadSupport;
import please.change.me.simulator.outgoing.CyclicDataReader;

import java.util.ArrayList;
import java.util.List;

/**
 * メッセージ送信シミュレータアクション。
 *
 * @author Ryo TANAKA
 * @since 1.4.2
 */
public class OutgoingSimulatorAction  extends FileBatchActionBase<DataRecord>
        implements DataReaderFactory<DataRecord> {
    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get(OutgoingSimulatorAction.class);

    /** 送信回数を指定するオプション */
    public static final String SEND_COUNT_OPTION = "sendCount";

    /** Excelからデータを読み取るクラス。 */
    private final MessageReadSupport messageReadSupport = new MessageReadSupport();


    /** {@inheritDoc} */
    public Result doData(DataRecord record, ExecutionContext context) {
        Result result = null;
        String synchronous = record.getString("synchronous");
        if (synchronous.toUpperCase().equals("TRUE")) {
            // 同期応答メッセージ送信
            result = synchronousSend(record, context);
        } else {
            // 同期不要メッセージ送信
            result = asynchronousSend(record, context);
        }
        return result;
    }


    /**
     * 同期応答送信。
     * @param record データレコード
     * @param context コンテキスト
     * @return 処理結果
     */
    public Result synchronousSend(DataRecord record, ExecutionContext context) {
        String sendMessageRequestId = record.getString("requestId");
        
        SyncMessage syncMessage = messageReadSupport.getSyncMessage(sendMessageRequestId);

        //送信内容のログを出力する
        if (LOGGER.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("SENT MESSAGE(in synchronousSend) ");
            sb.append("thread_name=[" + Thread.currentThread().getName() + "]. ");
            sb.append("request_id=[" + syncMessage.getRequestId() + "]. ");
            sb.append("header_record=[" + syncMessage.getHeaderRecord().toString() + "]. ");
            for (int i = 0; i < syncMessage.getDataRecords().size(); i++) {
                sb.append("data_record[" + i + "]=[" + syncMessage.getDataRecords().get(i).toString() + "]. ");
            }
            LOGGER.logInfo(sb.toString());
        }

        SyncMessage receiveSyncMessage = MessageSender.sendSync(syncMessage);
        
        //受信内容のログを出力する
        if (LOGGER.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("RECEIVED MESSAGE(in synchronousSend) ");
            sb.append("thread_name=[" + Thread.currentThread().getName() + "]. ");
            sb.append("request_id=[" + receiveSyncMessage.getRequestId() + "]. ");
            sb.append("header_record=[" + receiveSyncMessage.getHeaderRecord().toString() + "]. ");
            for (int i = 0; i < receiveSyncMessage.getDataRecords().size(); i++) {
                sb.append("data_record[" + i + "] = [" + receiveSyncMessage.getDataRecords().get(i).toString() + "]. ");
            }
            LOGGER.logInfo(sb.toString());
        }

        return new Success();
    }

    /**
     * 応答不要送信。
     * @param record データレコード
     * @param context コンテキスト
     * @return 処理結果
     */
    public Result asynchronousSend(DataRecord record, ExecutionContext context) {
        String sendMessageRequestId = record.getString("requestId");

        SendingMessage message = messageReadSupport.getSendingMessage(sendMessageRequestId);
        //送信内容のログを出力する
        if (LOGGER.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("SENT MESSAGE(in asynchronousSend) ");
            sb.append("thread_name=[" + Thread.currentThread().getName() + "]. ");
            sb.append("request_id=[" + sendMessageRequestId + "]. ");
            LOGGER.logInfo(sb.toString());
        }

        MessagingContext ctx = MessagingContext.getInstance();
        ctx.send(message);
        return new Success();
    }

    /** {@inheritDoc} */
    @Override
    public String getDataFileName() {
        return SystemRepository.getString("requests-to-send");
    }

    /** {@inheritDoc} */
    @Override
    public String getFormatFileName() {
        return "OutgoingRequest";
    }

    /**
     *
     * @param ctx 実行コンテキスト
     * @return データリーダ
     */
    @Override
    public DataReader<DataRecord> createReader(ExecutionContext ctx) {

        FileDataReader reader = new FileDataReader();
        reader.setDataFile(getDataFileDirName(), getDataFileName())
              .setLayoutFile(getFormatFileDirName(), getFormatFileName());

        List<DataRecord> dataRecords = new ArrayList<DataRecord>();
        while (reader.hasNext(ctx)) {
            DataRecord read = reader.read(ctx);
            dataRecords.add(read);
        }
        int sendCount = getSendCountOrElse(ctx, dataRecords.size());
        LOGGER.logInfo("send count=[" + sendCount + "].");

        return new CyclicDataReader(dataRecords, sendCount);
    }

    /**
     * 送信回数を取得する。
     * 送信回数をセッションスコープから取得する。この値はプログラム引数から引き継がれる。
     * 送信回数が指定されていない場合は、引数で与えられた代替値が返却される。
     *
     * @param ctx ExecutionContext
     * @param alternative 送信回数が明示的に指定されていない場合に使用する代替値
     * @return 送信回数
     */
    int getSendCountOrElse(ExecutionContext ctx, int alternative) {
        String sendCount = ctx.getSessionScopedVar(SEND_COUNT_OPTION);
        return sendCount == null ? alternative : Integer.parseInt(sendCount);
    }

}

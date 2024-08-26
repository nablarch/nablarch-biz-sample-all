package please.change.me.simulator.common;

import nablarch.core.dataformat.DataRecordFormatter;
import nablarch.core.dataformat.DataRecordFormatterSupport;
import nablarch.core.dataformat.FormatterFactory;
import nablarch.core.dataformat.InvalidDataFormatException;
import nablarch.core.util.FilePathSetting;
import nablarch.fw.messaging.FwHeader;
import nablarch.fw.messaging.MessageSenderSettings;
import nablarch.fw.messaging.ReceivedMessage;
import nablarch.fw.messaging.RequestMessage;
import nablarch.fw.messaging.ResponseMessage;
import nablarch.fw.messaging.SendingMessage;
import nablarch.fw.messaging.SyncMessage;
import nablarch.fw.messaging.realtime.http.client.HttpMessagingClient;
import nablarch.fw.web.HttpResponse;
import nablarch.test.core.reader.DataType;
import please.change.me.messaging.DummyFwHeaderDefinition;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * メッセージングシミュレータ用ファイル読み込みクラス。<br>
 * <p>
 * 取引単体用Excelフォーマットのファイルを、シミュレータ用のデータとして読み込む。<br>
 * </p>
 *
 * @author Masaya Seko
 * @since 1.4.2
 */
public class MessageReadSupport {

    /** 要求電文のデータフォーマット定義ファイル名パターン */
    private static final String REQUEST_MESSAGE_FORMAT_FILE_NAME_PATTERN = "%s" + "_SEND";

    /** {@link SendSyncSupportWrapper}実装クラス */
    private SendSyncSupportWrapper support;

    /**
     * コンストラクタ。
     */
    public MessageReadSupport() {
        this.support = SendSyncSupportWrapperConcurrent.getInstance();
    }

    /**
     * 同期送信(MessageSender用)の要求電文をファイルから取得する。
     *
     * @param requestId リクエストID(読み込むデータを表すID)
     * @return 要求電文
     */
    public SyncMessage getSyncMessage(String requestId) {
        byte[] responseMessageBinary = null;
        Map<String, Object> rawHeaderRecord = null;

        // Excelファイルから要求電文の本文を取得する
        responseMessageBinary = support.getResponseMessageBinaryByRequestId(DataType.EXPECTED_REQUEST_BODY_MESSAGES,
                                                                            requestId);


        // Excelファイルから要求電文のヘッダを取得する
        rawHeaderRecord = support.getResponseMessageByRequestId(DataType.EXPECTED_REQUEST_HEADER_MESSAGES,
                                                                requestId);

        Map<String, Object> headerRecord = new HashMap<>(rawHeaderRecord);
        //必ず付与される何番目のレコードか？を表す値を除去する。
        headerRecord.remove("DataFileFragment:firstFieldKey");

        String formatName = String.format(REQUEST_MESSAGE_FORMAT_FILE_NAME_PATTERN, requestId);
        SimpleDataConvertResult convertResult;
        try {
            convertResult = parseData(formatName, new ByteArrayInputStream(responseMessageBinary));
        } catch (IOException e) {
            //到達しない。
            throw new RuntimeException(e);
        }

        //要求電文用のオブジェクトを生成する。
        SyncMessage requestSyncMessage = new SyncMessage(requestId);
        requestSyncMessage.addDataRecord(convertResult.getResultMap());
        requestSyncMessage.setHeaderRecord(headerRecord);
        return requestSyncMessage;
    }

    /**
     * 応答なし送信(MessagingProvier用)の要求電文をファイルから取得する。
     *
     * @param requestId リクエストID(読み込むデータを表すID)
     * @return 要求電文
     */
    public SendingMessage getSendingMessage(String requestId) {
        Map<String, Object> bodyRecode = null;
        Map<String, Object> rawHeaderRecord = null;

        // Excelファイルから要求電文の本文を取得する
        bodyRecode = support.getResponseMessageByRequestId(DataType.EXPECTED_REQUEST_BODY_MESSAGES, requestId);

        // Excelファイルから要求電文のヘッダを取得する
        rawHeaderRecord = support.getResponseMessageByRequestId(DataType.EXPECTED_REQUEST_HEADER_MESSAGES, requestId);

        Map<String, Object> headerRecord = new HashMap<>(rawHeaderRecord);
        //必ず付与される何番目のレコードか？を表す値を除去する。
        headerRecord.remove("DataFileFragment:firstFieldKey");

        //送信定義ファイルの内容(messageSenderSettings.config)の情報を取得する
        MessageSenderSettings settings = new MessageSenderSettings(requestId);

        // 要求電文の生成
        SendingMessage sendingMessage = new SendingMessage();
        sendingMessage.setDestination(settings.getDestination());
        sendingMessage.setReplyTo(settings.getReplyTo());

        // 要求電文へフレームワーク制御ヘッダを設定する
        FwHeader fwHeader = new FwHeader();
        fwHeader.putAll(headerRecord);
        sendingMessage.setFormatter(settings.getHeaderFormatter());
        sendingMessage.addRecord("header", fwHeader);

        // 要求電文の本文の設定
        sendingMessage.setFormatter(settings.getSendingDataFormatter());
        sendingMessage.addRecord("data", bodyRecode);

        return sendingMessage;
    }

    /**
     * HTTP用応答電文をファイルから取得するために使用する。
     *
     * @param requestId リクエストID(読み込むデータを表すID)
     * @return HTTPResponse 応答に使用するHTTPResponse
     */
    public HttpResponse getResponseForHttp(String requestId) {
        byte[] bodyBytes = null;
        Map<String, Object> rawHeaderRecord = null;

        // Excelファイルから応答電文の本文を取得する
        bodyBytes = support.getResponseMessageBinaryByRequestId(DataType.RESPONSE_BODY_MESSAGES, requestId);


        // Excelファイルから応答電文のヘッダを取得する
        rawHeaderRecord = support.getResponseMessageByRequestId(DataType.RESPONSE_HEADER_MESSAGES, requestId);

        Map<String, Object> headerRecord = new HashMap<>(rawHeaderRecord);

        //ステータスコードをヘッダから読み取る(ヘッダから取得できなかった場合の初期として200を設定している)
        Integer statusCode = Integer.valueOf(200);
        String statusCodeString = (String) headerRecord.get(HttpMessagingClient.SYNCMESSAGE_STATUS_CODE);
        if (isNumber(statusCodeString)) {
            statusCode = Integer.parseInt(statusCodeString);
        }

        //「ヘッダから何番目のレコードか？を表す値」と、「ステータスコード用の値」を除去する。
        //(除去しない場合、HTTPヘッダに不要な情報が設定される)
        headerRecord.remove("DataFileFragment:firstFieldKey");
        headerRecord.remove(HttpMessagingClient.SYNCMESSAGE_STATUS_CODE);

        //応答を生成する。
        HttpResponse httpResponse = new HttpResponse();
        for (Entry<String, Object> entry : headerRecord.entrySet()) {
            httpResponse.setHeader(entry.getKey(), entry.getValue().toString());
        }
        httpResponse.setStatusCode(statusCode);
        httpResponse.setBodyStream(new ByteArrayInputStream(bodyBytes));
        return httpResponse;
    }

    /**
     * MOM用応答電文をファイル取得するために使用する。
     *
     * @param requestId       リクエストID(読み込むデータを表すID)
     * @param receivedMessage 受信したメッセージ
     * @return 応答電文
     */
    public ResponseMessage getMessageForMom(String requestId, ReceivedMessage receivedMessage) {
        byte[] headerBytes = null;
        byte[] bodyBytes = null;

        // Excelファイルから応答電文の本文を取得する
        bodyBytes = support.getResponseMessageBinaryByRequestId(DataType.RESPONSE_BODY_MESSAGES, requestId);

        // Excelファイルから応答電文のヘッダを取得する
        headerBytes = support.getResponseMessageBinaryByRequestId(DataType.RESPONSE_HEADER_MESSAGES, requestId);

        // ヘッダと本文のバイナリからバイト列を生成する
        int bufferSize = (headerBytes != null ? headerBytes.length : 0) + bodyBytes.length;
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        if (headerBytes != null) {
            buffer.put(headerBytes);
        }
        buffer.put(bodyBytes);
        byte[] responseBinary = buffer.array();

        //応答電文を生成する。
        RequestMessage req = new RequestMessage(null, receivedMessage);
        ResponseMessage reply = req.reply();
        reply.setFwHeaderDefinition(new DummyFwHeaderDefinition());
        reply.getBodyStream().write(responseBinary, 0, responseBinary.length);

        return reply;
    }

    /**
     * 引数が数値に変換可能か否か検証する。
     *
     * @param val 検証対象
     * @return trueの場合、変換可能
     */
    private boolean isNumber(String val) {
        try {
            Integer.parseInt(val);
            return true;
        } catch (NumberFormatException nfex) {
            return false;
        }
    }

    /**
     * 構造化データのストリームからMapを生成する。
     * 変換前の構造化データ形式はフォーマット定義ファイルにて指定される。
     *
     * @param formatName フォーマット定義ファイル
     * @param in 変換対象データ読み込み用ストリーム
     * @return 変換結果
     * @throws InvalidDataFormatException 入力データが不正な場合。
     * @throws IOException 読み込みに伴うIO処理で問題が発生した場合。
     */
    public static SimpleDataConvertResult parseData(String formatName, InputStream in) throws InvalidDataFormatException, IOException {
        // フォーマッタ取得
        DataRecordFormatter formatter = getFormatter(formatName);

        // データを解析
        formatter.setInputStream(in);
        formatter.initialize();
        Map<String, ?> resultMap = formatter.readRecord();
        formatter.close();

        return createResult(formatter)
                .setResultMap(resultMap);
    }

    /**
     * フォーマット名に対応したフォーマッタを取得する。
     *
     * @param formatName フォーマット名
     * @return フォーマッタ
     */
    private static DataRecordFormatter getFormatter(String formatName) {
        // フォーマットファイルを論理パスから取得
        File formatFile = FilePathSetting
                .getInstance()
                .getFileWithoutCreate("format", formatName);

        // フォーマッタを生成・初期化
        DataRecordFormatter formatter = FormatterFactory
                .getInstance()
                .createFormatter(formatFile);

        formatter.initialize();

        return formatter;
    }

    /**
     * 変換結果オブジェクトを生成する。
     *
     * @param formatter フォーマッタ
     * @return 変換結果
     */
    private static SimpleDataConvertResult createResult(DataRecordFormatter formatter) {
        SimpleDataConvertResult result = new SimpleDataConvertResult();

        // フォーマッタの各種設定値を取得
        // DataRecordFormatter実装クラスはDataRecordFormatterSupportのサブクラスであるので
        // キャストは成功する
        DataRecordFormatterSupport drfs = ((DataRecordFormatterSupport) formatter);
        result.setCharset(drfs.getDefaultEncoding());
        result.setDataType(drfs.getFileType());
        result.setMimeType(drfs.getMimeType());

        return result;
    }
}

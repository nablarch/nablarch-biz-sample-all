package please.change.me.simulator.common;

import nablarch.core.dataformat.DataRecord;
import nablarch.test.core.reader.DataType;

/**
 * {@link nablarch.test.core.messaging.SendSyncSupport}をラップするクラス。<br>
 * <p>
 * {@link nablarch.test.core.messaging.SendSyncSupport}をラップすることにより、以下の機能を付与している。<br>
 * <ul>
 * <li>メッセージ送受信に関する取引単体用テストデータをExcelから読み取る際に、最後の行まで読みきったら、最初の行から再び読み取る機能。</li>
 * </ul>
 * </p>
 * <p>
 * 以下の制限事項が存在する。
 * <ul>
 * <li>
 * {@link nablarch.test.core.messaging.SendSyncSupport}に存在していた、「Excelファイルの日付が更新されるとファイルを再読み込み」する機能は使用できない。
 * 本クラスを使用するプログラムを起動中は、Excelファイルの更新日付を変更してはならない。
 * </li>
 * </ul>
 * </p>
 * @author Masaya Seko
 * @since 1.4.2
 */
public interface SendSyncSupportWrapper {

    /**
     * リクエストIDに紐付くメッセージのバイナリを取得する。
     * @param dataType データタイプ
     * @param requestId リクエストID
     * @return 応答電文レコード
     */
    DataRecord getResponseMessageByRequestId(DataType dataType, String requestId);


    /**
     * リクエストIDに紐付くメッセージのバイナリを取得する。
     * @param dataType データタイプ
     * @param requestId リクエストID
     * @return メッセージ
     */
    byte[] getResponseMessageBinaryByRequestId(DataType dataType, String requestId);

    /**
     * 読み出し位置をリセットする。
     */
    void reset();
}

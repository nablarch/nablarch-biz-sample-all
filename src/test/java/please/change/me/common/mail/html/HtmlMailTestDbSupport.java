package please.change.me.common.mail.html;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Set;

import javax.sql.rowset.serial.SerialBlob;

import please.change.me.common.mail.html.entity.AlternativeText;
import please.change.me.common.mail.html.entity.AlternativeTextTemplate;
import please.change.me.common.mail.html.entity.BatchRequest;
import please.change.me.common.mail.html.entity.IdGenerateMail;
import please.change.me.common.mail.html.entity.MailAttachedFile;
import please.change.me.common.mail.html.entity.MailRecipient;
import please.change.me.common.mail.html.entity.MailRequest;
import please.change.me.common.mail.html.entity.MailTemplate;
import please.change.me.common.mail.html.entity.MailMessage;

import nablarch.common.mail.FreeTextMailContext;
import nablarch.common.mail.MailConfig;
import nablarch.core.date.SystemTimeUtil;
import nablarch.core.db.connection.AppDbConnection;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.statement.SqlPStatement;
import nablarch.core.db.statement.SqlResultSet;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.BinaryUtil;
import nablarch.core.util.StringUtil;
import nablarch.test.support.db.helper.VariousDbTestHelper;


/**
 * HtmlMail機能のDBアクセスを補助するクラス。
 *
 * @author tani takanori
 */
public class HtmlMailTestDbSupport {

    private String connectionName;

    /**
     * コネクション名なしで生成する。
     */
    public HtmlMailTestDbSupport() {
    }

    /**
     * 引数のコネクション名を元に生成する。
     *
     * @param connectionName コネクション名
     */
    public HtmlMailTestDbSupport(String connectionName) {
        this.connectionName = connectionName;
    }

    /**
     * DBを初期化する。
     * テーブルを構築し、テンプレートデータを事前に準備する。
     */
    static void initDB() {
        createTable();
        initMasterData();
    }

    /**
     * テーブルを作成する。
     */
    static void createTable() {
        VariousDbTestHelper.createTable(AlternativeText.class);
        VariousDbTestHelper.createTable(AlternativeTextTemplate.class);
        VariousDbTestHelper.createTable(BatchRequest.class);
        VariousDbTestHelper.createTable(IdGenerateMail.class);
        VariousDbTestHelper.createTable(MailAttachedFile.class);
        VariousDbTestHelper.createTable(MailRecipient.class);
        VariousDbTestHelper.createTable(MailRequest.class);
        VariousDbTestHelper.createTable(MailTemplate.class);
        VariousDbTestHelper.createTable(MailMessage.class);
    }

    /**
     * テーブルを削除する。
     */
    static void clearDb() {
        VariousDbTestHelper.dropTable(AlternativeText.class);
        VariousDbTestHelper.dropTable(AlternativeTextTemplate.class);
        VariousDbTestHelper.dropTable(BatchRequest.class);
        VariousDbTestHelper.dropTable(IdGenerateMail.class);
        VariousDbTestHelper.dropTable(MailAttachedFile.class);
        VariousDbTestHelper.dropTable(MailRecipient.class);
        VariousDbTestHelper.dropTable(MailRequest.class);
        VariousDbTestHelper.dropTable(MailTemplate.class);
        VariousDbTestHelper.dropTable(MailMessage.class);
    }

    /**
     * テストで利用するマスタデータテーブルを初期化する。
     */
    static void initMasterData() {
        // 採番ID
        MailConfig mailConfig = SystemRepository.get("mailConfig");
        String id = mailConfig.getMailRequestSbnId();
        VariousDbTestHelper.setUpTable(new IdGenerateMail(id, 0));
        // リクエストID
        VariousDbTestHelper.setUpTable(new BatchRequest("SENDMAIL00", "メール送信",
                "0", "0", "1"));
    }

    private AppDbConnection getAppConnection() {
        return StringUtil.isNullOrEmpty(connectionName) ? DbConnectionContext.getConnection()
                : DbConnectionContext.getConnection(connectionName);
    }

    /**
     * 関連テーブルを削除する。
     */
    protected void delete() {
        deleteRequest();
        deleteTemplate();
    }

    /**
     * メール送信要求を削除する。
     */
    protected void deleteRequest() {
        VariousDbTestHelper.delete(MailRequest.class);
        VariousDbTestHelper.delete(AlternativeText.class);
        VariousDbTestHelper.delete(MailRecipient.class);
        VariousDbTestHelper.delete(MailAttachedFile.class);
    }

    /**
     * メールテンプレートを削除する。
     *
     * @throws SQLException
     */
    protected void deleteTemplate() {
        VariousDbTestHelper.delete(MailTemplate.class);
        VariousDbTestHelper.delete(AlternativeTextTemplate.class);
    }

    // メール関連テーブルへの参照
    private static final String SELECT_REQUEST = "SELECT MAIL_REQUEST_ID, SUBJECT, MAIL_FROM, REPLY_TO, RETURN_PATH, CHARSET, STATUS, REQUEST_DATETIME, SEND_DATETIME, MAIL_BODY FROM MAIL_REQUEST";
    private static final String SELECT_HTML_ALT_TEXT = "SELECT MAIL_REQUEST_ID, ALTERNATIVE_TEXT FROM HTML_MAIL_ALT_TEXT ORDER BY MAIL_REQUEST_ID";

    /**
     * メール送信要求テーブルの情報を参照する。
     *
     * @return メール要求テーブルにあるデータ
     */
    protected SqlResultSet findMailRequest() {
        return getAppConnection().prepareStatement(SELECT_REQUEST).retrieve();
    }

    /**
     * HTMLメール送信要求テーブルの情報を取得する。
     *
     * @return HTMLメール送信要求テーブルにあるデータ
     */
    protected SqlResultSet findHtmlMailRequest() {
        return getAppConnection().prepareStatement(SELECT_HTML_ALT_TEXT).retrieve();
    }

    private static final String SELECT_ATTACHED_FILE = "SELECT MAIL_REQUEST_ID, SERIAL_NUMBER, CONTENT_TYPE, ATTACHED_FILE FROM MAIL_ATTACHED_FILE";

    /**
     * 添付ファイルテーブルの情報を取得する。
     *
     * @return
     */
    protected SqlResultSet findAttachedFile() {
        return getAppConnection().prepareStatement(SELECT_ATTACHED_FILE).retrieve();
    }

    /**
     * テストデータをメールリクエストテーブルに登録する。
     *
     * @param ctx 登録情報
     * @param mailRequestId メール要求ID
     * @param files 添付ファイル
     * @throws Exception 実行時の例外
     */
    protected void insertReqeustToSend(FreeTextMailContext ctx, String mailRequestId, FileData... files) throws Exception {
        // mail request
        MailConfig mailConfig = SystemRepository.get("mailConfig");
        VariousDbTestHelper.insert(new MailRequest(
                mailRequestId,
                ctx.getSubject(),
                ctx.getFrom(),
                ctx.getReplyTo(),
                ctx.getReturnPath(),
                ctx.getCharset(),
                mailConfig.getStatusUnsent(),
                ctx.getMailBody(),
                SystemTimeUtil.getTimestamp(),
                null
        ));

        // mail recipient
        int serialNum = 0;
        serialNum = addMailRecipient(mailRequestId, serialNum, mailConfig.getRecipientTypeTO(), ctx.getToList());
        serialNum = addMailRecipient(mailRequestId, serialNum, mailConfig.getRecipientTypeCC(), ctx.getCcList());
        addMailRecipient(mailRequestId, serialNum, mailConfig.getRecipientTypeBCC(), ctx.getBccList());

        // mail attached file
        insertAttachedFile(mailRequestId, files);
    }

    /**
     * パラメータを元にクエリを作成する。
     *
     * @param mailRequestId メール要求ID
     * @param serial 連番
     * @param recipientType 受信者種別
     * @param addresses アドレス
     * @return インクリメントした連番
     */
    private int addMailRecipient(String mailRequestId, int serial, String recipientType, Set<String> addresses) {
        for (String address : addresses) {
            serial++;
            VariousDbTestHelper.insert(new MailRecipient(
                    mailRequestId,
                    serial,
                    recipientType,
                    address
            ));
        }
        return serial;
    }

    /**
     * パラメータを基に添付ファイル用のデータを登録するクエリをステートメントに設定する。
     *
     * @param mailRequestId メール要求ID
     * @param files 添付ファイル
     * @throws SQLException SQLの実行例外
     * @throws IOException ファイルの読み込みなどで例外が発生した場合
     */
    private void insertAttachedFile(String mailRequestId, FileData... files) throws SQLException, IOException {
        for (int i = 0; i < files.length; i++) {
            FileData file = files[i];
            InputStream stream = null;
            try {
                stream = new FileInputStream(file.file);
                Blob blob = new SerialBlob(BinaryUtil.toByteArray(stream));
                VariousDbTestHelper.insert(new MailAttachedFile(
                        mailRequestId,
                        i,
                        file.name,
                        file.contentType,
                        blob
                ));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }
    }

    private static final String INSERT_HTML_ALT_TEXT = "INSERT INTO HTML_MAIL_ALT_TEXT (MAIL_REQUEST_ID, ALTERNATIVE_TEXT) values (?, ?)";

    /**
     * HTML用のメールリクエストを登録する。
     *
     * @param mailRequestId メール要求ID
     * @param alternativeText 代替テキスト
     */
    void insertHtmlRequest(String mailRequestId, String alternativeText) {
        SqlPStatement ps = getAppConnection().prepareStatement(INSERT_HTML_ALT_TEXT);
        ps.setString(1, mailRequestId);
        ps.setString(2, alternativeText);
        ps.execute();
    }

    /**
     * テンプレートを登録する。
     *
     * @param template メールテンプレート
     */
    protected void insertTemplate(HtmlMailTemplate template) {
        // TEMPLATE
        VariousDbTestHelper.insert(new MailTemplate(
                template.id,
                template.lang,
                template.subject,
                "UTF-8",
                template.body
        ));

        // HTML TEMPLATE
        if (template.alter != null) {
            VariousDbTestHelper.insert(new AlternativeTextTemplate(
                    template.id,
                    template.lang,
                    template.alter
            ));
        }
    }

    /**
     * メッセージをすべて削除する。
     */
    public void deleteMessage() {
        VariousDbTestHelper.delete(MailMessage.class);
    }

    /**
     * HTMLメールのテンプレートクラス。
     *
     * @author tani takanori
     */
    static class HtmlMailTemplate {

        private final String id;
        private final String lang;
        private final String body;
        private final String alter;
        private final String subject;
        HtmlMailTemplate(String id, String lang, String subject, String body, String alter) {
            this.id = id;
            this.lang = lang;
            this.subject = subject;
            this.body = body;
            this.alter = alter;
        }
    }

    /**
     * 添付ファイルの情報。
     *
     * @author tani takanori
     */
    static class FileData {
        private final String name;
        private final File file;
        private final String contentType;
        public FileData(String name, File file, String contentType) {
            this.name = name;
            this.file = file;
            this.contentType = contentType;
        }
    }

    /**
     * メッセージを登録する。<br />
     *
     * @param message_id メッセージID
     * @param message_ja メッセージ(ja)
     * @param message_en メッセージ(en)
     * @throws SQLException SQL実行時エラー
     */
    void insertMessage(String message_id, String message_ja, String message_en) throws SQLException {
        VariousDbTestHelper.insert(new MailMessage(message_id, "ja", message_ja));
        VariousDbTestHelper.insert(new MailMessage(message_id, "en", message_en));
    }
}

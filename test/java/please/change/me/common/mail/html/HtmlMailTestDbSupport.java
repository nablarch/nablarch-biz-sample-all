package please.change.me.common.mail.html;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import nablarch.common.mail.FreeTextMailContext;
import nablarch.common.mail.MailConfig;
import nablarch.core.date.SystemTimeUtil;
import nablarch.core.db.connection.AppDbConnection;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.statement.SqlPStatement;
import nablarch.core.db.statement.SqlResultSet;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.FileUtil;
import nablarch.core.util.StringUtil;
import oracle.jdbc.pool.OracleDataSource;


/**
 * HtmlMail機能のDBアクセスを補助するクラス。
 *
 * @author tani takanori
 */
public class HtmlMailTestDbSupport {

    private final MailConfig mailConfig = SystemRepository.get("mailConfig");

    private String connectionName = null;

    /**
     * プロセス上で一度だけ初期化できればいい。
     */
    private static boolean isReady = false;

    private static Connection con = null;

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
     * HTMLメール用のテーブルのDDLファイルのパス。
     */
    static final String DDL = "please/change/me/common/mail/html/drop_and_forward.sql";

    /**
     * DBを初期化する。
     * テーブルを構築し、テンプレートデータを事前に準備する。
     *
     * @throws SQLException コネクションの取得に失敗したとき。(ステートメント実行の失敗は発生しうるのでとめない。)
     * @throws IOException DDLファイルの読み込みに失敗したとき。
     */
    void initDB() throws SQLException, IOException {
        if (isReady) {
            return;
        }
        isReady = true;
        Connection con = getConnection();
        con.setAutoCommit(false);
        for (String sql : getDDLs()) {
            try {
                con.prepareStatement(sql).execute();
            } catch (SQLException ignore) {
                new RuntimeException(sql+"の実行で失敗",ignore).printStackTrace();
            }
        }
        initMasterData(con);
    }

    /**
     * テストで利用するマスタデータテーブルを初期化する。
     *
     * @param con コネクション
     * @throws SQLException SQL実行時のエラー
     */
    protected void initMasterData(Connection con) throws SQLException {
        // 採番ID
        MailConfig mailConfig = SystemRepository.get("mailConfig");
        String id = mailConfig.getMailRequestSbnId();
        PreparedStatement deleteId = con.prepareStatement("DELETE FROM ID_GENERATE_MAIL WHERE ID = ?");
        deleteId.setString(1, id);
        deleteId.execute();
        deleteId.close();
        PreparedStatement insertId = con.prepareStatement("INSERT INTO ID_GENERATE_MAIL (ID, NO) values (?, ?)");
        insertId.setString(1, id);
        insertId.setInt(2, 0);
        insertId.execute();
        insertId.close();
        // リクエストID
        PreparedStatement findRequest = con.prepareStatement("UPDATE BATCH_REQUEST SET REQUEST_ID = REQUEST_ID WHERE REQUEST_ID = 'SENDMAIL00'");
        if (findRequest.executeUpdate() == 0) {
            PreparedStatement insertRequest = con.prepareStatement(
                    "INSERT INTO BATCH_REQUEST (REQUEST_ID, REQUEST_NAME, PROCESS_HALT_FLG, PROCESS_ACTIVE_FLG, SERVICE_AVAILABLE)"
                                     + "values ('SENDMAIL00', 'メール送信', '0', '0', '1')");
            insertRequest.execute();
            insertRequest.close();
        }
        findRequest.close();
        con.commit();
    }

    /**
     * コネクションを取得する。
     *
     * @return DBコネクション
     * @throws SQLException コネクションの取得に失敗した場合
     */
    static Connection getConnection() throws SQLException {
        if (con == null || con.isClosed()) {
            ResourceBundle rb = ResourceBundle.getBundle("db-config");
            OracleDataSource ds = new OracleDataSource();
            ds.setURL(rb.getString("db.url"));
            ds.setUser(rb.getString("db.user"));
            ds.setPassword(rb.getString("db.password"));
            con = ds.getConnection();
        }
        return con;
    }

    private AppDbConnection getAppConnection() {
        return StringUtil.isNullOrEmpty(connectionName) ? DbConnectionContext.getConnection()
                                                        : DbConnectionContext.getConnection(connectionName);
    }

    /**
     * 関連テーブルを削除する。
     *
     * @param con DBコネクション
     * @throws SQLException SQL実行時のエラー
     * @see #deleteRequest(Connection)
     * @see #deleteTemplate(Connection)
     */
    protected void delete() throws SQLException {
        deleteRequest();
        deleteTemplate();
    }
    /**
     * メール送信要求を削除する。
     *
     * @param con DBコネクション
     * @throws SQLException SQL実行時のエラー
     */
    protected void deleteRequest() throws SQLException {
        Connection con = getConnection();
        for (String table : "MAIL_REQUEST, HTML_MAIL_ALT_TEXT, MAIL_RECIPIENT, MAIL_ATTACHED_FILE".split(",")) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM " + table);
            ps.execute();
            ps.close();
        }
        con.commit();
    }

    /**
     * メールテンプレートを削除する。
     *
     * @param con コネクション
     * @throws SQLException
     */
    protected void deleteTemplate() throws SQLException {
        Connection con = getConnection();
        for (String table : "MAIL_TEMPLATE, HTML_MAIL_ALT_TEXT_TEMPLATE".split(",")) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM " + table);
            ps.execute();
            ps.close();
        }
        con.commit();
    }

    // テンプレートへの登録
    private static final String INSERT_TEMPLATE = "INSERT INTO MAIL_TEMPLATE (MAIL_TEMPLATE_ID, LANG, SUBJECT, CHARSET, MAIL_BODY) values (?, ?, ?, ?, ?)";
    private static final String INSERT_HTML_ALT_TEXT_TEMPLATE = "INSERT INTO HTML_MAIL_ALT_TEXT_TEMPLATE (MAIL_TEMPLATE_ID, LANG, ALTERNATIVE_TEXT) values (?, ?, ?)";

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

    private static final String INSERT_REQUEST = "INSERT INTO MAIL_REQUEST (MAIL_REQUEST_ID, SUBJECT, MAIL_FROM, REPLY_TO, RETURN_PATH, CHARSET, STATUS, MAIL_BODY, REQUEST_DATETIME)"
                                               + " values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    /**
     * テストデータをメールリクエストテーブルに登録する。
     *
     * @param ctx 登録情報
     * @throws 実行時の例外
     */
    protected void insertReqeustToSend(FreeTextMailContext ctx, String mailRequestId, FileData ... files) throws Exception {
        AppDbConnection con = getAppConnection();
        // mail request
        SqlPStatement request = con.prepareStatement(INSERT_REQUEST);
        request.setString(1, mailRequestId);
        request.setString(2, ctx.getSubject());
        request.setString(3, ctx.getFrom());
        request.setString(4, ctx.getReplyTo());
        request.setString(5, ctx.getReturnPath());
        request.setString(6, ctx.getCharset());
        request.setString(7, mailConfig.getStatusUnsent());
        request.setString(8, ctx.getMailBody());
        request.setTimestamp(9, SystemTimeUtil.getTimestamp());
        request.execute();

        // mail recipient
        SqlPStatement ps = getAppConnection().prepareStatement(INSERT_RECIPIENT);
        int serialNum = 0;
        serialNum = addMailRecipient(mailRequestId, serialNum, mailConfig.getRecipientTypeTO(), ctx.getToList(), ps);
        serialNum = addMailRecipient(mailRequestId, serialNum, mailConfig.getRecipientTypeCC(), ctx.getCcList(), ps);
        addMailRecipient(mailRequestId, serialNum, mailConfig.getRecipientTypeBCC(), ctx.getBccList(), ps);
        ps.executeBatch();

        // mail attached file
        insertAttachedFile(mailRequestId, files);
    }

    private final String INSERT_RECIPIENT = "INSERT INTO MAIL_RECIPIENT (MAIL_REQUEST_ID, SERIAL_NUMBER, RECIPIENT_TYPE, MAIL_ADDRESS) values (?, ?, ?, ?)";

    /**
     * パラメータを元にクエリを作成する。
     *
     * @param mailRequestId メール要求ID
     * @param serial 連番
     * @param recipientType 受信者種別
     * @param addresses アドレス
     * @param ps ステートメント
     * @return インクリメントした連番
     */
    private int addMailRecipient(String mailRequestId, int serial, String recipientType, Set<String> addresses, SqlPStatement ps) {
        for (String address : addresses) {
            serial++;
            ps.setString(1, mailRequestId);
            ps.setString(2, String.valueOf(serial));
            ps.setString(3, recipientType);
            ps.setString(4, address);
            ps.addBatch();
        }
        return serial;
    }

    private final String INSERT_ATTACHE_FILE = "INSERT INTO MAIL_ATTACHED_FILE (MAIL_REQUEST_ID, SERIAL_NUMBER, FILE_NAME, CONTENT_TYPE, ATTACHED_FILE) values (?, ?, ? , ?, ?)";

    /**
     * パラメータを基に添付ファイル用のデータを登録するクエリをステートメントに設定する。
     *
     * @param mailRequestId メール要求ID
     * @param files 添付ファイル
     * @throws IOException ファイルの読み込みなどで例外が発生した場合
     */
    private void insertAttachedFile(String mailRequestId, FileData ... files) throws IOException {
        SqlPStatement ps = getAppConnection().prepareStatement(INSERT_ATTACHE_FILE);
        for (int i = 0; i < files.length; i++) {
            FileData file = files[i];
            InputStream stream = null;
            try {
                stream = new FileInputStream(file.file);
                ps.setString(1, mailRequestId);
                ps.setString(2, String.valueOf(i));
                ps.setString(3, file.name);
                ps.setString(4, file.contentType);
                ps.setBinaryStream(5, stream, stream.available());
                ps.execute();
            } catch (IOException e) {
                throw new RuntimeException("an error occurred while reading file:", e);
            } finally {
                stream.close();
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
     * @throws SQLException SQL実行時のエラー
     */
    protected void insertTemplate(MailTemplate template) throws SQLException {
        // TEMPLATE
        PreparedStatement ps = con.prepareStatement(INSERT_TEMPLATE);
        ps.setString(1, template.id);
        ps.setString(2, template.lang);
        ps.setString(3, template.subject);
        ps.setString(4, "UTF-8");
        ps.setString(5, template.body);
        ps.execute();

        // HTML TEMPLATE
        if (template.alter != null) {
            ps = con.prepareStatement(INSERT_HTML_ALT_TEXT_TEMPLATE);
            ps.setString(1, template.id);
            ps.setString(2, template.lang);
            ps.setString(3, template.alter);
            ps.execute();
        }
        con.commit();
    }

    /**
     * HTMLメールのテンプレートクラス。
     *
     * @author tani takanori
     */
    static class MailTemplate {
        private final String id;
        private final String lang;
        private final String body;
        private final String alter;
        private final String subject;
        MailTemplate(String id, String lang,String subject ,String body, String alter) {
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
     * DDLを定義したファイルからDDLを読み取る。<br />
     * 返却するDDL文のリストは記載された順に並ぶ。(DROP、CREATE、ALTERなどでソートは行わない)<br />
     *
     * '-' で開始される行はコメントとして読み飛ばし、改行ではなく、';'で文を区切る。(文に';'は含まれない)
     *
     * @return
     * @throws IOException
     */
    private static List<String> getDDLs() throws IOException {
        InputStream in = FileUtil.getClasspathResource(DDL);
        if (in == null) {
            throw new IllegalStateException(DDL + " was not found in classpath.");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        try {
            String line = reader.readLine();
            while (line != null) {
                if (!isSkip(line)) {
                    sb.append(line);
                }
                line = reader.readLine();
            }
        } finally {
            FileUtil.closeQuietly(reader);
        }
        return Arrays.asList(sb.toString().split(";"));
    }

    /**
     * 読み込んだ行を読み飛ばすかどうか。
     *
     * @param line 行
     * @return コメント('-'で開始している)の場合のみ true
     */
    private static boolean isSkip(String line) {
        return line.startsWith("-");
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
        PreparedStatement ps = con.prepareStatement("DELETE FROM MESSAGE WHERE MESSAGE_ID = ?");
        ps.setString(1, message_id);
        ps.execute();
        ps = con.prepareStatement("INSERT INTO MESSAGE (MESSAGE_ID, LANG, MESSAGE) values (?, ?, ?)");
        ps.setString(1, message_id);
        ps.setString(2, "ja");
        ps.setString(3, message_ja);
        ps.execute();
        ps.setString(1, message_id);
        ps.setString(2, "en");
        ps.setString(3, message_en);
        ps.execute();
        con.commit();
    }

    void close() throws SQLException {
        if (con != null && !con.isClosed()) {
            con.close();
        }
    }
}

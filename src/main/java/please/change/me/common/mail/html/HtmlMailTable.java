package please.change.me.common.mail.html;

import nablarch.core.db.connection.AppDbConnection;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.statement.SqlPStatement;
import nablarch.core.db.statement.SqlResultSet;
import nablarch.core.db.statement.SqlRow;

/**
 * HTMLメール用のメール関連テーブルクラス。
 *
 * @author tani takanori
 */
public class HtmlMailTable {

    /**
     * HTMLメールの代替テキストテーブルに登録するINSERT文。
     */
    private static final String INSERT_ALTERNATIVE_TEXT = "INSERT INTO HTML_MAIL_ALT_TEXT (MAIL_REQUEST_ID, ALTERNATIVE_TEXT) VALUES (?,?)";
    /**
     * HTMLメールの代替テキストテーブルから代替テキストを取得するSELECT文。
     */
    private static final String SELECT_ALTERNATIVE_TEXT = "SELECT ALTERNATIVE_TEXT FROM HTML_MAIL_ALT_TEXT WHERE MAIL_REQUEST_ID = ?";
    /**
     * HTMLメールの代替テキストテンプレートテーブルから代替テキストを取得するSELECT文。
     */
    private static final String SELECT_ALTERNATIVE_TEXT_TEMPLATE = "SELECT ALTERNATIVE_TEXT FROM HTML_MAIL_ALT_TEXT_TEMPLATE " 
                                                                 + "WHERE MAIL_TEMPLATE_ID = ? AND LANG = ?";

    /**
     * 代替テキストのテンプレートを取得する。<br />
     *
     * 存在しない場合はnullを返却する。
     *
     * @param templateId テンプレートID
     * @param lang 言語
     * @return テンプレート情報
     */
    public SqlRow findAlternativeTextTemplate(String templateId, String lang) {
        SqlPStatement statement = getConnection().prepareStatement(SELECT_ALTERNATIVE_TEXT_TEMPLATE);
        statement.setString(1, templateId);
        statement.setString(2, lang);
        SqlResultSet rs = statement.retrieve();
        return rs.isEmpty() ? null : rs.get(0);
    }

    /**
     * メールリクエストIDからHTMLメール用の代替テキストを取得する。<br />
     *
     * 存在しない場合はnullを返却する。
     *
     * @param mailRequestId メールリクエストID
     * @return メール要求IDに紐づくHTML用データ。存在しない場合はnullなので検証後に取得すること。
     */
    public SqlRow findAlternativeText(String mailRequestId) {
        AppDbConnection connection = getConnection();
        SqlPStatement ps = connection.prepareStatement(SELECT_ALTERNATIVE_TEXT);
        ps.setString(1, mailRequestId);
        SqlResultSet rs = ps.retrieve();
        return rs.isEmpty() ? null : rs.get(0);
    }

    /**
     * HTMLメール用の代替テキストをメールリクエストに関連づけて登録する。
     *
     * @param mailRequestId メールリクエストID
     * @param ctx メールコンテキスト
     */
    public void insertAlternativeText(String mailRequestId, TemplateHtmlMailContext ctx) {
        AppDbConnection connection = getConnection();
        SqlPStatement ps = connection.prepareStatement(INSERT_ALTERNATIVE_TEXT);
        ps.setString(1, mailRequestId);
        ps.setString(2, ctx.getAlternativeText());
        ps.executeUpdate();
    }

    /**
     * DBのコネクションを取得する。
     *
     * @return {@link AppDbConnection}
     */
    protected AppDbConnection getConnection() {
        return DbConnectionContext.getConnection();
    }
}

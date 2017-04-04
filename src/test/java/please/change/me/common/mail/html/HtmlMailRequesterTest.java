package please.change.me.common.mail.html;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import nablarch.common.mail.MailRequester;
import nablarch.common.mail.MailUtil;
import nablarch.common.mail.TemplateMailContext;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.statement.SqlPStatement;
import nablarch.core.db.statement.SqlResultSet;
import nablarch.core.db.statement.SqlRow;
import nablarch.test.support.db.helper.DatabaseTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * {@link HtmlMailRequester}の単体テスト。
 *
 * @author tani takanori
 */
@RunWith(DatabaseTestRunner.class)
public class HtmlMailRequesterTest extends HtmlMailTestSupport {
    private final MailRequester target = MailUtil.getMailRequester();
    private final HtmlMailTestDbSupport db = new HtmlMailTestDbSupport();

    /**
     * テストのデフォルトの内容を設定する。
     *
     */
    @Before
    public void setUpDb() {
        db.delete();
        db.insertTemplate(new HtmlMailTestDbSupport.HtmlMailTemplate("1", "ja", "{name}さんへのお知らせ" ,"メール本文. [key : {key}]", "代替テキスト {key}"));
        db.insertTemplate(new HtmlMailTestDbSupport.HtmlMailTemplate("1", "en", "langが異なる {name}" ,"langが異なる本文. [key : {key}]", "alter text {key}"));
        db.insertTemplate(new HtmlMailTestDbSupport.HtmlMailTemplate("2", "ja", "IDが異なる {name}" ,"IDが異なる本文{name}. [key : {key}]", "IDが異なる代替テキスト {key}"));
        db.insertTemplate(new HtmlMailTestDbSupport.HtmlMailTemplate("2", "en", "IDとlangが異なる {name}" ,"IDとlangが異なる本文{name}. [key : {key}]", "IDが異なる代替テキスト {key}"));
        db.insertTemplate(new HtmlMailTestDbSupport.HtmlMailTemplate("3", "ja", "代替テキストなし(ja) {name}" ,"代替テキストなし {name}. [key : {key}]", null));
        db.insertTemplate(new HtmlMailTestDbSupport.HtmlMailTemplate("3", "en", "代替テキストなし(en) {name}" ,"代替テキストなし {name}. [key : {key}]", null));
    }

    /**
     * {@link HtmlMailRequester#requestToSend(TemplateMailContext)}で要求を出した場合のテスト。<br />
     * <ul>
     * <li>指定したテンプレートが利用されること。</li>
     * <li>プレースホルダが変換されること。</li>
     * </ul>
     * </pre>
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void testRequestToSendByPlain() throws Exception {
        TemplateMailContext ctx = new TemplateMailContext();
        ctx.setFrom("from");
        ctx.setLang("ja");
        ctx.addTo("test.address@test.com");
        ctx.setReturnPath("return.path");
        ctx.setReplyTo("reply.to");
        ctx.setTemplateId("1");
        ctx.setReplaceKeyValue("key", "case1");
        ctx.setReplaceKeyValue("name", "user");
        String mailRequestId = target.requestToSend(ctx);
        commit();

        SqlResultSet request = db.findMailRequest();
        assertThat(request.size(), is(1));
        SqlRow row = request.get(0);
        assertThat(row.getString("mailRequestId"), is(mailRequestId));
        assertThat(row.getString("subject"), is("userさんへのお知らせ"));
        assertThat(row.getString("mailBody"), is("メール本文. [key : case1]"));
        assertThat(row.getString("returnPath"), is("return.path"));
        assertThat(row.getString("replyTo"), is("reply.to"));

        SqlResultSet htmlRequest = db.findHtmlMailRequest();
        assertThat(htmlRequest.isEmpty(), is(true));
    }

    /**
     * {@link HtmlMailRequester#requestToSend(TemplateMailContext)}でHTML形式の要求を出した場合のテスト。<br />
     * <ul>
     *  <li>指定したテンプレートが利用されること。</li>
     *  <li>HTMLメール送信要求テーブルに情報が格納されること</li>
     * </ul>
     *
     * @throws Exception 想定外の例外が発生した場合。
     */
    @Test
    public void testRequestToSendByHtml() throws Exception {
        TemplateHtmlMailContext ctx = new TemplateHtmlMailContext();
        ctx.setFrom("from");
        ctx.setLang("ja");
        ctx.addTo("test.address@test.com");
        ctx.setReturnPath("return.path");
        ctx.setReplyTo("reply.to");
        ctx.setTemplateId("1");
        ctx.setReplaceKeyValue("key", "case2");
        ctx.setReplaceKeyValue("name", "user");
        ctx.setContentType(ContentType.HTML);
        String mailRequestId = target.requestToSend(ctx);
        commit();

        SqlResultSet request = db.findMailRequest();
        assertThat(request.size(), is(1));
        SqlRow row = request.get(0);
        assertThat(row.getString("mailRequestId"), is(mailRequestId));
        assertThat(row.getString("subject"), is("userさんへのお知らせ"));
        assertThat(row.getString("mailBody"), is("メール本文. [key : case2]"));
        assertThat(row.getString("returnPath"), is("return.path"));
        assertThat(row.getString("replyTo"), is("reply.to"));

        SqlResultSet htmlRequest = db.findHtmlMailRequest();
        assertThat(htmlRequest.size(), is(1));
        row = htmlRequest.get(0);
        assertThat(row.getString("mailRequestId"), is(mailRequestId));
        assertThat(row.getString("alternativeText"), is("代替テキスト case2"));
    }

    /**
     * {@link HtmlMailRequester#requestToSend(TemplateMailContext)}でプレーン形式の要求を出した場合のテスト。<br />
     * <ul>
     *  <li>指定したテンプレートが利用されること。</li>
     *  <li>HTMLメール送信要求テーブルに情報が格納されること</li>
     * </ul>
     *
     * @throws Exception 想定外の例外が発生した場合。
     */
    @Test
    public void testRequestToSendByPlainText() throws Exception {
        TemplateHtmlMailContext ctx = new TemplateHtmlMailContext();
        ctx.setFrom("from");
        ctx.setLang("ja");
        ctx.addTo("test.address@test.com");
        ctx.setReturnPath("return.path");
        ctx.setReplyTo("reply.to");
        ctx.setTemplateId("1");
        ctx.setReplaceKeyValue("key", "switch");
        ctx.setReplaceKeyValue("name", "user");
        ctx.setContentType(ContentType.PLAIN);
        String mailRequestId = target.requestToSend(ctx);
        commit();

        SqlResultSet request = db.findMailRequest();
        assertThat(request.size(), is(1));
        SqlRow row = request.get(0);
        assertThat(row.getString("mailRequestId"), is(mailRequestId));
        assertThat(row.getString("subject"), is("userさんへのお知らせ"));
        assertThat(row.getString("mailBody"), is("代替テキスト switch"));
        assertThat(row.getString("returnPath"), is("return.path"));
        assertThat(row.getString("replyTo"), is("reply.to"));

        SqlResultSet htmlRequest = db.findHtmlMailRequest();
        assertThat(htmlRequest.size(), is(0));
    }


    /**
     * 言語を判定してテンプレートを利用することの確認。
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void testRequestToSendLang() throws Exception {
        TemplateHtmlMailContext ctx = new TemplateHtmlMailContext();
        ctx.setFrom("from");
        ctx.setLang("en");
        ctx.addTo("test.address@test.com");
        ctx.setReturnPath("return.path");
        ctx.setReplyTo("reply.to");
        ctx.setTemplateId("1");
        ctx.setReplaceKeyValue("key", "case_lang");
        ctx.setReplaceKeyValue("name", "user");
        String mailRequestId = target.requestToSend(ctx);
        commit();

        SqlResultSet request = db.findMailRequest();
        assertThat(request.size(), is(1));
        SqlRow row = request.get(0);
        assertThat(row.getString("mailRequestId"), is(mailRequestId));
        assertThat(row.getString("subject"), is("langが異なる user"));
        assertThat(row.getString("mailBody"), is("langが異なる本文. [key : case_lang]"));
        assertThat(row.getString("returnPath"), is("return.path"));
        assertThat(row.getString("replyTo"), is("reply.to"));

        SqlResultSet htmlRequest = db.findHtmlMailRequest();
        assertThat(htmlRequest.size(), is(1));
        row = htmlRequest.get(0);
        assertThat(row.getString("mailRequestId"), is(mailRequestId));
        assertThat(row.getString("alternativeText"), is("alter text case_lang"));
    }

    /**
     * HTML形式で指定したが、HTML用のテンプレートが存在しない場合の確認。
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void testHtmlMailTemplateNotExists() throws Exception {
        String templateId = "3";
        assertTrue(notExistsInHtmlMailTemplate(templateId));

        TemplateHtmlMailContext ctx = new TemplateHtmlMailContext();
        ctx.setFrom("from");
        ctx.setLang("ja");
        ctx.addTo("test.address@test.com");
        ctx.setReturnPath("return.path");
        ctx.setReplyTo("reply.to");
        ctx.setTemplateId(templateId);
        ctx.setReplaceKeyValue("key", "case3");
        ctx.setReplaceKeyValue("name", "user");
        try {
            target.requestToSend(ctx);
            fail("指定したテンプレートがなければ例外が発生する。");
        } catch (IllegalArgumentException expected) {
            String message = expected.getMessage();
            assertThat("メッセージにテンプレートIDが含まれる。", message, containsString("templateId = 3"));
            assertThat("メッセージに言語が含まれる。", message, containsString("lang = ja"));
        }
    }

    /**
     * HTML形式で指定したが、HTML用のテンプレートが存在しない場合の確認(言語を変えた場合の確認)。
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void testHtmlMailTemplateNotExistsAsPlain() throws Exception {
        String templateId = "3";
        assertTrue(notExistsInHtmlMailTemplate(templateId));

        TemplateHtmlMailContext ctx = new TemplateHtmlMailContext();
        ctx.setFrom("from");
        ctx.setLang("en");
        ctx.addTo("test.address@test.com");
        ctx.setReturnPath("return.path");
        ctx.setReplyTo("reply.to");
        ctx.setTemplateId(templateId);
        ctx.setReplaceKeyValue("key", "case3");
        ctx.setReplaceKeyValue("name", "user");
        ctx.setContentType(ContentType.PLAIN);
        try {
            target.requestToSend(ctx);
            fail("指定したテンプレートがなければ例外が発生する。");
        } catch (IllegalArgumentException expected) {
            String message = expected.getMessage();
            assertThat("メッセージにテンプレートIDが含まれる。", message, containsString("templateId = 3"));
            assertThat("メッセージに言語が含まれる。", message, containsString("lang = en"));
        }
    }


    /**
     * HTMLメールテンプレートの存在チェック。
     *
     * @param templateId テンプレートID
     * @return HTMLメールテンプレートに存在するかどうか
     */
    boolean notExistsInHtmlMailTemplate(String templateId) {
        SqlPStatement ps = DbConnectionContext.getConnection().prepareStatement("SELECT * FROM HTML_MAIL_ALT_TEXT_TEMPLATE WHERE MAIL_TEMPLATE_ID = ?");
        ps.setString(1, templateId);
        return ps.retrieve().isEmpty();
    }
}

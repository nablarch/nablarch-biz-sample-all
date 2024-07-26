package please.change.me.common.mail.html;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import nablarch.core.db.statement.SqlResultSet;
import nablarch.core.db.statement.SqlRow;
import nablarch.test.support.db.helper.DatabaseTestRunner;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import please.change.me.common.mail.html.HtmlMailTestDbSupport.HtmlMailTemplate;

/**
 * {@link HtmlMailTable}の単体テストクラス。
 *
 * @author tani takanori
 *
 */
@RunWith(DatabaseTestRunner.class)
public class HtmlMailTableTest extends HtmlMailTestSupport {
    private final HtmlMailTestDbSupport db = new HtmlMailTestDbSupport();

    private final HtmlMailTable target = new HtmlMailTable();


    /**
     * メールリクエスト、テンプレートを削除する。
     *
     */
    @Override
    @After
    public void tearDown() {
        super.tearDown();
        db.delete();
    }

    /**
     * HtmlMailTableAccessor#getTemplate(String, String) のテスト。
     *
     */
    @Test
    public void testGetTemplate() {
        db.insertTemplate(new HtmlMailTemplate("1", "ja", "件名", "本文", "代替テキスト"));
        db.insertTemplate(new HtmlMailTemplate("1", "en", "subject", "body", "alter text"));

        assertThat("KEYが違えばnull[ID]", target.findAlternativeTextTemplate("2", "ja"), is(nullValue()));
        assertThat("KEYが違えばnull[LANG]", target.findAlternativeTextTemplate("1", "ea"), is(nullValue()));
        SqlRow ja = target.findAlternativeTextTemplate("1", "ja");
        assertThat("キーが一致したものが取得できる", ja.getString("alternativeText"), is("代替テキスト"));


        SqlRow en = target.findAlternativeTextTemplate("1", "en");
        assertThat("キーが一致したものが取得できる", en.getString("alternativeText"), is("alter text"));
    }

    /**
     * HtmlMailTableAccessor#insertHtmlRequest(String, TemplateHtmlMailContext) のテスト。
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void testInsertRequest() throws Exception {
        assertThat("初期は0件", db.findHtmlMailRequest().size(), is(0));

        TemplateHtmlMailContext ctx = new TemplateHtmlMailContext();
        ctx.setAlternativeText("代替テキスト");
        target.insertAlternativeText("1", ctx);
        SqlResultSet requests = db.findHtmlMailRequest();
        assertThat("登録したら1件", requests.size(), is(1));
        assertThat("設定した代替テキストが利用される。", requests.get(0).getString("alternativeText"), is("代替テキスト"));

        ctx.setAlternativeText("その２");
        target.insertAlternativeText("2", ctx);
        requests = db.findHtmlMailRequest();
        assertThat("再度登録したら2件", requests.size(), is(2));
        assertThat("設定した代替テキストがりようされる。", requests.get(1).getString("alternativeText"), is("その２"));
    }

    /**
     * HtmlMailTableAccessor#getHtmlMailRequest(String requestId)
     *
     * @throws Exception 想定外の例外が発生した場合。
     */
    @Test
    public void testFindRequest() throws Exception {
        assertThat("存在しない場合はnull", target.findAlternativeText("1"), is(nullValue()));
        db.insertHtmlRequest("1", "代替テキスト");
        SqlRow request = target.findAlternativeText("1");
        assertThat("存在しない場合はnull", request, not(nullValue()));
        assertThat("登録した内容が取得できる", request.getString("alternativeText"), is("代替テキスト"));
    }

}

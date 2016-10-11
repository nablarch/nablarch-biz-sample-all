package please.change.me.common.mail.html;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

/**
 * {@link TemplateHtmlMailContext}の単体テスト。
 *
 * @author tani takanori
 */
public class TemplateHtmlMailContextTest {

    /**
     * TemplateHtmlMailContextのsetter/getterのテスト。
     */
    @Test
    public void testSetterGetter() {
        TemplateHtmlMailContext target = new TemplateHtmlMailContext();

        assertThat(target.getAlternativeText(), is(""));
        target.setAlternativeText("alternativeText");
        assertThat(target.getAlternativeText(), is("alternativeText"));

        assertThat(target.getContentType(), is(ContentType.HTML));
        target.setContentType("dummy");
        assertThat(target.getContentType(), is("dummy"));

        Map<String, String> replaceKeyValue = target.getReplaceKeyValue();
        assertThat(replaceKeyValue.size(), is(0));
        target.setReplaceKeyValue("key", "value");
        assertThat(replaceKeyValue.containsKey("key"), is(true));
        assertThat(replaceKeyValue.get("key"), is("value"));
    }

    /**
     * コンテンツタイプの指定で、代替テキストをメール本文に変換する機能の確認。
     */
    @Test
    public void testSwitchPlain() {
        TemplateHtmlMailContext ctx = new TemplateHtmlMailContext();
        final String alternativeText = "this is alternative-text";
        ctx.setAlternativeText(alternativeText);
        assertThat("CONTENT-TYPEの初期値はHTML", ctx.getContentType(), is(ContentType.HTML));
        assertThat("ボディーは設定していないのでNULL", ctx.getMailBody(), is(nullValue()));
        ctx.setContentType(ContentType.PLAIN);
        assertThat("CONTENT-TYPEをPlainにするとAlterがBodyに変わる。", ctx.getMailBody(), is(alternativeText));
    }

    /**
     * コンテンツタイプをPlainに指定した際にalternativeTextが設定されてい場合の確認。
     *
     * @throws Exception 想定外の例外が発生した場合。
     */
    @Test
    public void testSwitchPlainFail() throws Exception {
        TemplateHtmlMailContext target = new TemplateHtmlMailContext();
        assertThat("初期値はHTMLコンテンツがHTML", target.getContentType(), is(ContentType.HTML));
        assertThat("初期値はHTMLコンテンツがHTMLなので、例外は発生しない。", target.getMailBody(), is(nullValue()));

        target.setContentType(ContentType.PLAIN);
        target.setAlternativeText("some text");
        assertThat("代替テキストが設定されているなら例外は起きない。", target.getMailBody(), is("some text"));

        target.setAlternativeText("");
        try {
            target.getMailBody();
            fail("異常な状態のため、例外となるはず。");
        } catch (IllegalStateException expected) {
            String message = expected.getMessage();
            assertThat("メッセージにContentTypeが含まれること", message, is("failed to get alternative text. if content type is plain, must set alternative text."));
        }
    }

    /**
     * 埋めこみ文字がエスケープされることの確認。
     */
    @Test
    public void testEscapeHtmlInSetReplaceValue() {
        TemplateHtmlMailContext ctx = new TemplateHtmlMailContext();
        Map<String, String> target = ctx.getReplaceKeyValue();

        assertThat(target.isEmpty(), is(true));

        ctx.setReplaceKeyValue("notEscape white space", "value has white space");
        assertThat(target.get("notEscape white space"), is("value has white space"));

        ctx.setReplaceKeyValue("escape start tag", "<img");
        assertThat(target.get("escape start tag"), is("&lt;img"));

        ctx.setReplaceKeyValue("holder in tag attr", "< may escaped");
        assertThat("エスケープ用のやつを利用しているのでエスケープされる。", target.get("holder in tag attr"), is("&lt; may escaped"));

    }

    /**
     * {@link TemplateHtmlMailContext#hasAlternativeText()}のテスト。
     *
     * @throws Exception 想定外の例外が発生した場合。
     */
    @Test
    public void testHasAlternativeText() throws Exception {
        TemplateHtmlMailContext target = new TemplateHtmlMailContext();
        target.setAlternativeText(null);
        assertThat("nullの場合、false", target.hasAlternativeText(), is(false));

        target.setAlternativeText("");
        assertThat("emptyの場合、false", target.hasAlternativeText(), is(false));

        target.setAlternativeText("alter text");
        assertThat("nullでもemptyでもない場合、true", target.hasAlternativeText(), is(true));
    }

    /**
     * {@link TemplateHtmlMailContext#isPlainTextMail()}のテスト。
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void testIsPlainTextMail() throws Exception {
        TemplateHtmlMailContext target = new TemplateHtmlMailContext();

        assertThat("デフォルトはfalse", target.isPlainTextMail(), is(false));

        target.setContentType(ContentType.PLAIN);
        assertThat("PLAINを指定したらtrueになる", target.isPlainTextMail(), is(true));

        target.setContentType(ContentType.HTML);
        assertThat("HTMLを指定したらfalseになる。", target.isPlainTextMail(), is(false));
    }
}

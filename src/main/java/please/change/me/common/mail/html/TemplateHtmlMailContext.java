package please.change.me.common.mail.html;

import static nablarch.common.web.HtmlTagUtil.escapeHtml;
import nablarch.common.mail.TemplateMailContext;
import nablarch.core.util.StringUtil;

/**
 * HTML形式をサポートするメール用の定型メールコンテキスト。
 *
 * @author tani takanori
 */
public class TemplateHtmlMailContext extends TemplateMailContext {

    /**
     * 代替テキスト
     */
    private String alternativeText = "";

    /**
     * コンテンツ種別。
     */
    private String contentType = ContentType.HTML;

    /**
     * メール本文を取得する。<br />
     * HTML用のテンプレートを利用したプレーンテキストメール機能のため、
     * このコンテキストに{@link ContentType#PLAIN}を指定されている場合、
     * {@link #getAlternativeText()}と同じ値(マークアップのないテキスト)を返却する。<br />
     *
     * {@link #isPlainTextMail()}がtrueで{@link #hasAlternativeText()}がfalseの場合、状態不正のため、例外を送出する。
     *
     * @return Mailの本文
     */
    @Override
    public String getMailBody() {
        // 明示的にPLAINを指定した場合、代替テキストを本文とする。
        if (isPlainTextMail()) {
            if (!hasAlternativeText()) {
                throw new IllegalStateException("failed to get alternative text. if content type is plain, must set alternative text.");
            }
            return getAlternativeText();
        }
        return super.getMailBody();
    }

    /**
     * 代替テキストを取得する。
     *
     * @return 代替テキスト
     */
    public String getAlternativeText() {
        return this.alternativeText;
    }

    /**
     * 代替テキストを設定する。
     *
     * @param alternativeText 代替テキスト
     */
    public void setAlternativeText(String alternativeText) {
        this.alternativeText = alternativeText;
    }

    /**
     * コンテンツタイプを取得する。
     *
     * @return コンテンツタイプ
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * コンテンツタイプを設定する。
     *
     * @param contentType コンテンツタイプ
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * {@inheritDoc}<br />
     * このメソッドでは、置き換え前にhtmlエスケープを行う。<br />
     */
    @Override
    public void setReplaceKeyValue(String key, String value) {
        super.setReplaceKeyValue(key, escapeHtml(value));
    }

    /**
     * 代替テキストを保持しているかどうか判定する。
     *
     * @return 代替テキストが設定されている場合、ture
     */
    public boolean hasAlternativeText() {
        return StringUtil.hasValue(alternativeText);
    }

    /**
     * プレーンテキスト形式のコンテンツタイプが指定されているかどうか判定する。
     *
     * @return プレーンテキスト形式であればtrue
     */
    public boolean isPlainTextMail() {
        return ContentType.PLAIN.equals(contentType);
    }
}

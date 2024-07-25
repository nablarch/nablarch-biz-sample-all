package please.change.me.common.mail.html;

import java.util.Map;
import java.util.Map.Entry;

import nablarch.common.mail.MailRequester;
import nablarch.common.mail.TemplateMailContext;
import nablarch.core.db.statement.SqlRow;

/**
 * TEXT, TEXT+HTMLのメールに対応した {@link MailRequester}の拡張。<br>
 *
 * 代替テキストをテンプレートから取得し、要求として保存する。<br />
 *
 * @author tani takanori
 */
public class HtmlMailRequester extends MailRequester {

    /**
     * HtmlMail関連テーブル。
     */
    private HtmlMailTable htmlMailTable;

    /**
     * 関連テーブルへのアクセスを行うインスタンスを設定する。
     *
     * @param htmlMailTable 関連テーブルへのアクセスクラス。
     */
    public void setHtmlMailTable(HtmlMailTable htmlMailTable) {
        this.htmlMailTable = htmlMailTable;
    }

    /**
     * テンプレートを利用してメール送信要求を登録する。
     * HTML用のテンプレートが存在し、contentTypeが{@link ContentType#HTML}の場合HTMLメールとして要求テーブルに格納する。
     *
     * @param ctx コンテキスト
     * @return メール要求ID
     */
    @Override
    public String requestToSend(TemplateMailContext ctx) {
        if (!(ctx instanceof TemplateHtmlMailContext)) {
            return super.requestToSend(ctx);
        }

        TemplateHtmlMailContext htmlCtx = (TemplateHtmlMailContext) ctx;
        /*
         * ContextTypeがPLAINの場合、ctx.getMailBody()にてalternativeTextをmailBodyとして利用するため
         * super.requestToSend(ctx)を呼び出す前に、あらかじめ代替テキストを設定しておく。
         */
        setAlternativeText(htmlCtx);

        String id = super.requestToSend(htmlCtx);

        addHtmlInfo(htmlCtx, id);
        return id;
    }

    /**
     * HTMLメールの情報(代替テキスト)を関連テーブルに登録する。<br />
     * HTMLメールのコンテキスト情報のコンテンツタイプがプレーンテキストに指定されている、または代替テキストが設定されていない場合は何もしない。
     *
     * @param ctx HTMLメールのコンテキスト情報
     * @param requestId メール送信要求ID
     */
    protected void addHtmlInfo(TemplateHtmlMailContext ctx, String requestId) {
        if (ctx.isPlainTextMail()) {
            return;
        }
        htmlMailTable.insertAlternativeText(requestId, ctx);
    }

    /**
     * HTMLメール用の代替テキストのテンプレートを元にして、HTMLメール用コンテキストに代替テキストを設定する。<br />
     *
     * @param ctx HTMLメールコンテキスト。
     */
    protected void setAlternativeText(TemplateHtmlMailContext ctx) {
        SqlRow template = htmlMailTable.findAlternativeTextTemplate(ctx.getTemplateId(), ctx.getLang());
        if (template == null) {
            throw new IllegalArgumentException(String.format("alternative text template is not found. templateId = %s, lang = %s",
                                                             ctx.getTemplateId(), ctx.getLang()));
        }
        ctx.setAlternativeText(replaceTemplateString(ctx.getReplaceKeyValue(), template.getString("alternativeText")));
    }

    /**
     * テンプレートの文字列を置換する。
     *
     * @param replaceKeyValue プレースホルダと置換文字列のマップ
     * @param targetStr 置換対象文字列
     * @return 置換後の文字列
     */
    private String replaceTemplateString(Map<String, String> replaceKeyValue, String targetStr) {
        for (Entry<String, String> entry : replaceKeyValue.entrySet()) {
            targetStr = targetStr.replace('{' + entry.getKey() + '}', entry.getValue());
        }
        return targetStr;
    }
}

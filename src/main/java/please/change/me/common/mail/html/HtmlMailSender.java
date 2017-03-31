package please.change.me.common.mail.html;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import nablarch.common.mail.MailAttachedFileTable;
import nablarch.common.mail.MailRequestTable;
import nablarch.common.mail.MailSender;
import nablarch.core.db.statement.SqlRow;
import nablarch.core.repository.SystemRepository;
import nablarch.fw.ExecutionContext;

/**
 * TEXT, HTML+TEXTを送信するメール送信バッチ。
 *
 * @author tani takanori
 */
public class HtmlMailSender extends MailSender {

    /**
     * メール送信要求からボディ部を作成し、MimeMessageに追加する。
     *
     * @param mimeMessage {@link MimeMessage}
     * @param mailRequest メール送信要求管理の情報
     * @param attachedFiles 添付ファイルの情報
     * @param context 実行コンテキスト
     * @throws MessagingException メールメッセージの生成に失敗した場合
     */
    @Override
    protected void addBodyContent(MimeMessage mimeMessage, MailRequestTable.MailRequest mailRequest,
             List<? extends MailAttachedFileTable.MailAttachedFile> attachedFiles,
             ExecutionContext context) throws MessagingException {

        HtmlMailTable htmlTable = SystemRepository.get("htmlMailTable");
        SqlRow htmlMailRequest = htmlTable.findAlternativeText(mailRequest.getMailRequestId());

        if (htmlMailRequest == null) {
            super.addBodyContent(mimeMessage, mailRequest, attachedFiles, context);
            return;
        }

        mimeMessage.setContent(HtmlMailContentCreator.create(mailRequest.getMailBody(), mailRequest.getCharset(),
                                                             htmlMailRequest.getString("alternativeText"), attachedFiles));
    }
}

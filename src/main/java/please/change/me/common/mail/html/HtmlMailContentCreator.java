package please.change.me.common.mail.html;

import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import nablarch.common.mail.MailAttachedFileTable;

/**
 * HTMLメールに対応するContentを生成するクラス。
 *
 * @author tani takanori
 */
public final class HtmlMailContentCreator {

    /** インスタンス抑止 */
    private HtmlMailContentCreator() {
    }

    /**
     * コンテンツを生成する。
     *
     * @param mailBody メールのbody
     * @param charset 文字セット
     * @param alternativeText 代替テキスト
     * @param attachedFiles 添付ファイルのリスト
     * @return 生成したマルチパート
     * @throws MessagingException MimePartを操作した際の実行時例外
     */
    public static Multipart create(String mailBody, String charset, String alternativeText,
                  List<? extends MailAttachedFileTable.MailAttachedFile> attachedFiles) throws MessagingException {
        MimeMultipart textOnly = new MimeMultipart("alternative");
        textOnly.addBodyPart(toAlternativePart(alternativeText, charset), 0);
        textOnly.addBodyPart(toHtmlPart(mailBody, charset), 1);
        return addFile(textOnly, attachedFiles);
    }

    /**
     * 添付ファイルを追加する。
     * 添付ファイルがない場合は、引数のMimeMultiPartを返却する。
     *
     * @param textOnly テキスト部
     * @param attachedFiles 添付ファイルのリスト
     * @return 添付ファイルの設定処理をしたMimeMultiPart
     * @throws MessagingException MimePartを操作した際の実行時例外
     */
    private static Multipart addFile(MimeMultipart textOnly,
                                    List<? extends MailAttachedFileTable.MailAttachedFile> attachedFiles) throws MessagingException {
        if (attachedFiles.isEmpty()) {
            return textOnly;
        }
        MimeMultipart mixed = new MimeMultipart("mixed");
        MimeBodyPart textBody = new MimeBodyPart();
        textBody.setContent(textOnly);
        mixed.addBodyPart(textBody);

        for (MailAttachedFileTable.MailAttachedFile attachedFile : attachedFiles) {
            mixed.addBodyPart(toFilePart(attachedFile));
        }
        return mixed;
    }

    /**
     * HTML部の形式に変換する。
     *
     * @param mailBody メール本文
     * @param charset 文字セット
     * @return HTML部のボディーパート
     * @throws MessagingException MimeBodyPartにテキストを設定したときの実行時例外
     */
    private static BodyPart toHtmlPart(String mailBody, String charset) throws MessagingException {
        MimeBodyPart html = new MimeBodyPart();
        html.setText(mailBody, charset, "html");
        return html;
    }

    /**
     * 代替テキスト部の形式に変換する。
     *
     * @param alternativeText 代替テキスト
     * @param charset 文字セット
     * @return 代替テキスト部のボディーパート
     * @throws MessagingException MimeBodyPartにテキストを設定したときの実行時例外
     */
    private static BodyPart toAlternativePart(String alternativeText, String charset) throws MessagingException {
        MimeBodyPart alternative = new MimeBodyPart();
        alternative.setText(alternativeText, charset, "plain");
        return alternative;
    }

    /**
     * 添付ファイル部の形式に変換する。
     *
     * @param attachedFile アタッチファイル
     * @return 添付ファイル部
     * @throws MessagingException MimePartを操作した際の実行時例外
     */
    private static MimeBodyPart toFilePart(MailAttachedFileTable.MailAttachedFile attachedFile) throws MessagingException {
        DataSource dataSource = new ByteArrayDataSource(attachedFile.getFile(), attachedFile.getContextType());
        DataHandler dataHandler = new DataHandler(dataSource);

        MimeBodyPart filePart = new MimeBodyPart();
        filePart.setDataHandler(dataHandler);
        filePart.setFileName(attachedFile.getFileName());
        return filePart;
    }
}

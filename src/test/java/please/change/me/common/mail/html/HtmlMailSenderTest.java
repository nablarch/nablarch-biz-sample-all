package please.change.me.common.mail.html;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import nablarch.common.mail.FreeTextMailContext;
import nablarch.common.mail.MailAttachedFileTable;
import nablarch.common.mail.MailAttachedFileTable.MailAttachedFile;
import nablarch.common.mail.MailRequestTable;
import nablarch.common.mail.MailRequestTable.MailRequest;
import nablarch.core.db.statement.SqlResultSet;
import nablarch.core.repository.SystemRepository;
import nablarch.fw.ExecutionContext;
import nablarch.test.support.db.helper.DatabaseTestRunner;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import please.change.me.common.mail.html.HtmlMailTestDbSupport.FileData;

/**
 * {@link HtmlMailSender}のテスト。
 *
 * @author tani takanori
 */
@RunWith(DatabaseTestRunner.class)
public class HtmlMailSenderTest extends HtmlMailTestSupport {

    private final HtmlMailTestDbSupport db = new HtmlMailTestDbSupport();
    private final MailRequestTable requestTable = SystemRepository.get("mailRequestTable");
    private final MailAttachedFileTable fileTable = SystemRepository.get("mailAttachedFileTable");

    private static final String CHARSET = "UTF-8";
    private static final String FROM = "from";
    private static final String MAIL_BODY = "body";
    private static final String REPLY_TO = "replyTo";
    private static final String RETURN_PATH = "returnPath";
    private static final String SUBJECT = "subject";
    private static final String ATTACHED_FILE_TYPE = "text/file"; // dummy.
    private static final String ALTER_TEXT = "alter_text";

    /**
     * 添付ファイルを作成するためのフォルダ。
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * データベースのレコードを削除する。
     *
     */
    @Override
    @After
    public void tearDown() {
        super.tearDown();
        db.delete();
    }

    /**
     * HTMLメール送信要求がない場合は、プレーンテキストが設定されることを確認する。<br />
     *
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void testAddBodyContentPlainText() throws Exception {
        db.insertReqeustToSend(makeDefaultRequest(), "1");
        commit();

        // 事前条件
        SqlResultSet requestDatas = db.findMailRequest();
        assertThat("メール送信要求は1件だけ", requestDatas.size(), is(1));
        assertThat("添付ファイルもない", db.findAttachedFile().size(), is(0));
        assertThat("HTMLのメール送信要求はない。", db.findHtmlMailRequest().size(), is(0));

        Session session = Session.getInstance(new Properties());
        MimeMessage message = new MimeMessage(session);

        MailRequest mailRequest = requestTable.getMailRequest(requestDatas.get(0));

        new HtmlMailSender().addBodyContent(message, mailRequest, new ArrayList<MailAttachedFile>(), new ExecutionContext());
        message.saveChanges();
        assertThat(message.getContentType(), containsString("text/plain"));
        assertThat(message.getContent(), instanceOf(String.class));
        assertThat(message.getContent().toString(), is("body"));
    }

    /**
     * HTMLメール送信要求がなく、添付ファイルが存在する場合のMimeMessageの確認。
     *
     * @throws Exception 想定外の例外が発生した場合。
     */
    @Test
    public void testAddBodyContentAttachedFile() throws Exception {
        final String mailRequestId = "2";
        FreeTextMailContext ctx = makeDefaultRequest();
        File temp = createTempfile("temp.txt");
        FileData file = new HtmlMailTestDbSupport.FileData("temp.txt", temp, ATTACHED_FILE_TYPE);
        db.insertReqeustToSend(ctx, mailRequestId, file);
        commit();

        // 事前条件
        SqlResultSet requestDatas = db.findMailRequest();
        assertThat("メール送信要求は1件だけ", requestDatas.size(), is(1));
        assertThat("HTMLのメール送信要求はない。", db.findHtmlMailRequest().size(), is(0));
        List<MailAttachedFile> attachedFiles = fileTable.find(mailRequestId);
        assertThat("添付ファイルがある。", attachedFiles.size(), is(1));

        Session session = Session.getInstance(new Properties());
        MimeMessage message = new MimeMessage(session);

        new HtmlMailSender().addBodyContent(message, requestTable.getMailRequest(requestDatas.get(0)), attachedFiles, new ExecutionContext());
        message.saveChanges();
        assertThat(message.getContentType(), containsString("multipart/mixed"));
        assertThat("コンテンツはMultiPart", message.getContent(), instanceOf(Multipart.class));
        Multipart part = (Multipart) message.getContent();
        assertThat("Mimeに含まれるコンテンツの数。", part.getCount(), is(2));
        for (int i = 0; i < part.getCount(); i++ ) {
            BodyPart bodyPart = part.getBodyPart(i);
            String disposition = bodyPart.getDisposition();
            if (Part.ATTACHMENT.equals(disposition)) {
                assertThat("添付ファイルのコンテンツタイプ", bodyPart.getContentType(), containsString(ATTACHED_FILE_TYPE));
                assertThat("添付ファイルのファイル名", bodyPart.getFileName(), is(temp.getName()));
            } else {
                assertThat("本文のcharset", bodyPart.getContentType(), containsString(CHARSET));
                assertThat("本文のコンテンツタイプ", bodyPart.getContentType(), containsString("text/plain"));
                assertThat("本文", bodyPart.getContent().toString(), is(MAIL_BODY));
            }
        }
    }

    /**
     * HTMLテキストのMimeMessageの確認。
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void testAddBodyContentHtmlText() throws Exception {
        final String mailRequestId = "3";
        db.insertReqeustToSend(makeDefaultRequest(), mailRequestId);
        db.insertHtmlRequest(mailRequestId, ALTER_TEXT);
        commit();

        // 事前条件
        SqlResultSet requestDatas = db.findMailRequest();
        assertThat("メール送信要求は1件だけ", requestDatas.size(), is(1));
        SqlResultSet htmlRequest = db.findHtmlMailRequest();
        assertThat("HTMLのメール送信要求がある", htmlRequest.size(), is(1));
        assertThat("メール送信要求にひもづいている。", htmlRequest.get(0).getString("mailRequestId"), is(mailRequestId));
        List<MailAttachedFile> attachedFiles = fileTable.find(mailRequestId);
        assertThat("添付ファイルはない。", attachedFiles.size(), is(0));

        Session session = Session.getInstance(new Properties());
        MimeMessage message = new MimeMessage(session);

        new HtmlMailSender().addBodyContent(message, requestTable.getMailRequest(requestDatas.get(0)), attachedFiles, new ExecutionContext());

        message.saveChanges();
        assertThat("コンテンツタイプ", message.getContentType(), containsString("multipart/alternative"));
        assertThat("コンテンツはMultiPart", message.getContent(), instanceOf(Multipart.class));
        Multipart part = (Multipart) message.getContent();
        assertThat("Mimeに含まれる要素の数", part.getCount(), is(2));
        assertAlternativePart(part);
    }

    /**
     * 添付ファイル有のHTMLテキストのMimeMessageの確認。
     *
     * @throws Exception 想定外の例外が発生した場合。
     */
    @Test
    public void testAddBodyContentHtmlWithAttachedFiles() throws Exception {
        final String mailRequestId = "4";
        FreeTextMailContext ctx = makeDefaultRequest();
        FileData temp1 = new FileData("temp1.txt", createTempfile("temp1.txt"), ATTACHED_FILE_TYPE);
        FileData temp2 = new FileData("temp2.txt", createTempfile("temp2.txt"), ATTACHED_FILE_TYPE);

        db.insertReqeustToSend(ctx, mailRequestId, temp1, temp2);
        db.insertHtmlRequest(mailRequestId, ALTER_TEXT);
        commit();

        // 事前条件
        SqlResultSet requestDatas = db.findMailRequest();
        assertThat("メール送信要求は1件だけ", requestDatas.size(), is(1));
        SqlResultSet htmlRequest = db.findHtmlMailRequest();
        assertThat("HTMLのメール送信要求がある", htmlRequest.size(), is(1));
        assertThat("メール送信要求にひもづいている。", htmlRequest.get(0).getString("mailRequestId"), is(mailRequestId));

        List<MailAttachedFile> attachedFiles = fileTable.find(mailRequestId);
        assertThat("添付ファイルがある。", attachedFiles.size(), is(2));

        Session session = Session.getInstance(new Properties());
        MimeMessage message = new MimeMessage(session);

        new HtmlMailSender().addBodyContent(message, requestTable.getMailRequest(requestDatas.get(0)), attachedFiles, new ExecutionContext());
        message.saveChanges();
        assertThat("コンテンツタイプ", message.getContentType(), containsString("multipart/mixed"));
        assertThat("コンテンツはMultiPart", message.getContent(), instanceOf(Multipart.class));
        Multipart part = (Multipart) message.getContent();
        assertThat("Mimeに含まれる要素の数((html+text),attache x 2)", part.getCount(), is(3));
        for (int i = 0; i < part.getCount(); i++) {
            BodyPart bodyPart = part.getBodyPart(i);
            String contentType = bodyPart.getContentType();
            if (contentType.contains("multipart/alternative")) {
                assertThat("マルチパートである", bodyPart.getContent(), instanceOf(Multipart.class));
                assertAlternativePart((Multipart)bodyPart.getContent());
            } else if (Part.ATTACHMENT.equals(bodyPart.getDisposition())) {
                assertThat("ファイル名", bodyPart.getFileName().matches("temp[12].txt"), is(true));
            } else {
                fail("想定外のコンテンツが含まれている。 Content-type" + contentType);
            }
        }
    }

    /**
     * HTML+TEXT形式の検証を行う。
     *
     * @param part alternativeのパート
     * @throws Exception 実行時の例外
     */
    private void assertAlternativePart(Multipart part) throws Exception {
        assertThat(part.getContentType(), containsString("multipart/alternative"));
        assertThat("HTMLとTEXTがふくまれること", part.getCount(), is(2));

        BodyPart bodyPart = part.getBodyPart(0);
        String contentType = bodyPart.getContentType();
        assertThat("一部のメーラーが後勝ちなので、代替テキストが上部にあること", contentType,  containsString("text/plain"));
        assertThat("代替テキストのcharset", contentType, containsString(CHARSET));
        assertThat("代替テキスト", bodyPart.getContent().toString(), is(ALTER_TEXT));

        bodyPart = part.getBodyPart(1);
        contentType = bodyPart.getContentType();
        assertThat("一部のメーラーが後勝ちなので、HTMLが下部にあること", contentType,  containsString("text/html"));
        assertThat("本文のcharset", contentType, containsString(CHARSET));
        assertThat("本文", bodyPart.getContent().toString(), is(MAIL_BODY));
    }

    /**
     * テストで利用するデフォルトのメール送信要求を作成する。
     *
     * @return 非定型のメールコンテキスト
     */
    private FreeTextMailContext makeDefaultRequest() {
        FreeTextMailContext ctx = new FreeTextMailContext();
        ctx.setCharset(CHARSET);
        ctx.setFrom(FROM);
        ctx.setMailBody(MAIL_BODY);
        ctx.setReplyTo(REPLY_TO);
        ctx.setReturnPath(RETURN_PATH);
        ctx.setSubject(SUBJECT);
        return ctx;
    }

    private File createTempfile(String fileName) throws IOException {
        File temp = folder.newFile(fileName);
        FileWriter writer = new FileWriter(temp);
        writer.write("test");
        writer.close();
        return temp;
    }
}

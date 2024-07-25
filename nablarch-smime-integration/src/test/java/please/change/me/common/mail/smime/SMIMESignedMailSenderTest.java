package please.change.me.common.mail.smime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

import java.io.File;
import java.util.List;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;

import please.change.me.common.mail.testsupport.MailTestSupport;
import please.change.me.common.mail.testsupport.entity.MailSendRequest;

import nablarch.common.mail.AttachedFile;
import nablarch.common.mail.FreeTextMailContext;
import nablarch.fw.launcher.CommandLine;
import nablarch.fw.launcher.Main;
import nablarch.test.support.db.helper.DatabaseTestRunner;
import nablarch.test.support.db.helper.VariousDbTestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * {@link SMIMESignedMailSender}のテスト。
 *
 * @author hisaaki sioiri
 */
@RunWith(DatabaseTestRunner.class)
public class SMIMESignedMailSenderTest extends MailTestSupport {

    /** メールサーバへの接続プロパティ */
    private static final Properties MAIL_SESSION_PROPERTIES = new Properties();

    /** メール受信時のウェイト時間 */
    private static final int WAIT_TIME = 5000;

    static {
        MAIL_SESSION_PROPERTIES.setProperty("mail.smtp.host", "localhost");
        MAIL_SESSION_PROPERTIES.setProperty("mail.host", "localhost");
        MAIL_SESSION_PROPERTIES.setProperty("mail.pop3.host", "localhost");
        MAIL_SESSION_PROPERTIES.setProperty("mail.pop3.port", "10110");
    }

    @Before
    public void setupTestCase() throws Exception {
        cleanupMail("to1");
        cleanupMail("cc1");
        cleanupMail("bcc1");
    }

    /**
     * テスト用のメールフォルダをクリーニングする。
     *
     * @param account クリーニング対象のメールアカウント
     * @throws Exception
     */
    private void cleanupMail(final String account) throws Exception {
        Session session = Session.getInstance(MAIL_SESSION_PROPERTIES, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(account, "default");
            }
        });

        Store store = session.getStore("pop3");
        store.connect();
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);
        Message[] messages = folder.getMessages();
        System.out.println("account " + account + ": " + messages.length + " messages will be deleted.");
        for (int i = 0; i < messages.length; i++) {
            messages[i].setFlag(Flags.Flag.DELETED, true);
        }
        folder.close(true);
        store.close();
    }

    private Message[] getMailMessage(final String account) throws Exception {
        Session session = Session.getInstance(MAIL_SESSION_PROPERTIES, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(account, "default");
            }
        });

        Store store = session.getStore("pop3");
        store.connect();
        Folder folder = store.getFolder("INBOX");
        for (int i = 0; i < 10; i++) {
            folder.open(Folder.READ_WRITE);
            Message[] messages = folder.getMessages();
            if (messages.length >= 1) {
                return messages;
            }
            folder.close(true);
            Thread.sleep(WAIT_TIME);
        }
        return null;
    }

    /**
     * 添付ファイル無しパターンの電子署名確認。
     */
    @Test
    public void testSmimeNoAttachedFile() throws Exception {

        FreeTextMailContext mailContext = new FreeTextMailContext();
        mailContext.setSubject("けんめい");
        mailContext.setMailBody("本文");
        mailContext.setCharset("utf-8");
        mailContext.setFrom("from@from.com");
        mailContext.addTo("to1@localhost");
        mailContext.addCc("cc1@localhost");
        mailContext.addBcc("bcc1@localhost");
        mailContext.setMailSendPatternId("01");
        mailRequest(mailContext);
        // 処理対象外
        mailContext.setMailSendPatternId("00");
        mailRequest(mailContext);
        // 処理対象外
        mailContext.setMailSendPatternId("02");
        mailRequest(mailContext);

        // バッチ実行
        CommandLine commandLine = new CommandLine("-diConfig",
                "please/change/me/common/mail/smime/SmimeSignedMailSenderTest.xml", "-requestPath",
                "please.change.me.common.mail.smime.SMIMESignedMailSender/SENDMAIL00", "-userId", "userid",
                "-mailSendPatternId", "01");
        int exitCode = Main.execute(commandLine);

        assertThat(exitCode, is(0));

        // ログアサート
        assertLog("メール送信要求が 1 件あります。");
        assertLog("メール送信完了。メールリクエストID 101");

        // 送信したメッセージの確認
        Message[] toMessages = getMailMessage("to1");
        Message[] ccMessages = getMailMessage("cc1");
        Message[] bccMessages = getMailMessage("bcc1");

        // メッセージが1つう送信されていること
        assertThat(toMessages, is(notNullValue()));
        assertThat(toMessages.length, is(1));
        assertThat(ccMessages, is(notNullValue()));
        assertThat(ccMessages.length, is(1));
        assertThat(bccMessages, is(notNullValue()));
        assertThat(bccMessages.length, is(1));

        // 各メッセージ部をアサート
        for (Message message : new Message[]{toMessages[0], ccMessages[0], bccMessages[0]}) {
            // メッセージ共通部のアサート
            assertThat(message.getContentType(), is(containsString("multipart/signed;")));
            assertThat(message.getContentType(), is(containsString("application/pkcs7-signature")));
            assertThat(message.getContentType(), is(containsString("micalg=sha-1;")));
            assertThat(message.getFrom().length, is(1));
            assertThat((InternetAddress) message.getFrom()[0], is(new InternetAddress("from@from.com")));

            assertThat(message.getContent(), is(instanceOf(Multipart.class)));
            assertThat(message.getSubject(), is("けんめい"));

            //----------------------------------------------------------------------
            // 本文部分のアサート
            //----------------------------------------------------------------------
            Multipart multipart = (Multipart) message.getContent();
            BodyPart body = multipart.getBodyPart(0);
            assertThat(body.getContentType(), is(containsString("text/plain")));
            assertThat(body.getContentType(), is(containsString("charset=utf-8")));
            assertThat((String) body.getContent(), is("本文"));

            //----------------------------------------------------------------------
            // 電子署名部のアサート
            //----------------------------------------------------------------------
            BodyPart smime = multipart.getBodyPart(1);
            assertThat(smime.getFileName(), is("smime.p7s"));

        }

        List<MailSendRequest> requests = VariousDbTestHelper.findAll(MailSendRequest.class, "mailRequestId");
        assertThat("リクエスト数が一致すること", requests.size(), is(3));
        verifyRequestStatus( requests.get(0), "101", "B", true);
        verifyRequestStatus( requests.get(1), "102", "A", false);
        verifyRequestStatus( requests.get(2), "103", "A", false);
    }

    /**
     * 添付ファイル有りパターンの電子署名確認。
     */
    @Test
    public void testSmimeAttachedFile() throws Exception {

        FreeTextMailContext mailContext = new FreeTextMailContext();
        mailContext.setSubject("けんめい");
        mailContext.setMailBody("本文");
        mailContext.setCharset("utf-8");
        mailContext.setFrom("from@from.com");
        mailContext.addTo("to1@localhost");
        mailContext.addCc("cc1@localhost");
        mailContext.addBcc("bcc1@localhost");
        mailContext.setMailSendPatternId("01");
        mailContext.addAttachedFile(new AttachedFile("text/plain", new File("src/test/resources/please/change/me/common/mail/smime/data/temp1.txt")));
        mailRequest(mailContext);
        // 処理対象外
        mailContext.setMailSendPatternId("00");
        mailRequest(mailContext);
        // 処理対象外
        mailContext.setMailSendPatternId("02");
        mailRequest(mailContext);

        // バッチ実行
        CommandLine commandLine = new CommandLine("-diConfig",
                "please/change/me/common/mail/smime/SmimeSignedMailSenderTest.xml", "-requestPath",
                "please.change.me.common.mail.smime.SMIMESignedMailSender/SENDMAIL00", "-userId", "userid",
                "-mailSendPatternId", "01");
        int exitCode = Main.execute(commandLine);

        assertThat(exitCode, is(0));

        // ログアサート
        assertLog("メール送信要求が 1 件あります。");
        assertLog("メール送信完了。メールリクエストID 101");

        // 送信したメッセージの確認
        Message[] toMessages = getMailMessage("to1");
        Message[] ccMessages = getMailMessage("cc1");
        Message[] bccMessages = getMailMessage("bcc1");

        // メッセージが1つう送信されていること
        assertThat(toMessages, is(notNullValue()));
        assertThat(toMessages.length, is(1));
        assertThat(ccMessages, is(notNullValue()));
        assertThat(ccMessages.length, is(1));
        assertThat(bccMessages, is(notNullValue()));
        assertThat(bccMessages.length, is(1));

        // 各メッセージ部をアサート
        for (Message message : new Message[]{toMessages[0], ccMessages[0], bccMessages[0]}) {

            // メッセージ共通部のアサート
            assertThat(message.getContentType(), is(containsString("multipart/signed;")));
            assertThat(message.getContentType(), is(containsString("application/pkcs7-signature")));
            assertThat(message.getContentType(), is(containsString("micalg=sha-1;")));
            assertThat(message.getFrom().length, is(1));
            assertThat((InternetAddress) message.getFrom()[0], is(new InternetAddress("from@from.com")));

            assertThat(message.getContent(), is(instanceOf(Multipart.class)));
            assertThat(message.getSubject(), is("けんめい"));

            //----------------------------------------------------------------------
            // 本文部分のアサート
            //----------------------------------------------------------------------
            Multipart multipart = (Multipart) message.getContent();
            assertThat(multipart.getCount(), is(2));
            BodyPart body = multipart.getBodyPart(0);
            assertThat(body.getContentType(), is(containsString("multipart/mixed")));
            assertThat(body.getContent(), is(instanceOf(Multipart.class)));
            Multipart bodyPart = (Multipart) body.getContent();

            // 本文部は、本文と添付ファイルの2つ
            assertThat(bodyPart.getCount(), is(2));
            BodyPart bodyText = bodyPart.getBodyPart(0);
            assertThat(bodyText.getContentType(), is(containsString("text/plain")));
            assertThat(bodyText.getContentType(), is(containsString("charset=utf-8")));
            assertThat((String) bodyText.getContent(), is("本文"));

            BodyPart file = bodyPart.getBodyPart(1);
            assertThat(file.getFileName(), is("temp1.txt"));
            assertThat(file.getContentType(), is(containsString("text/plain")));
            assertThat((String) file.getContent(), is("l-1\r\nl-2\r\n"));

            //----------------------------------------------------------------------
            // 電子署名部のアサート
            //----------------------------------------------------------------------
            BodyPart smime = multipart.getBodyPart(1);
            assertThat(smime.getFileName(), is("smime.p7s"));
        }

        List<MailSendRequest> requests = VariousDbTestHelper.findAll(MailSendRequest.class, "mailRequestId");
        assertThat("リクエスト数が一致すること", requests.size(), is(3));
        verifyRequestStatus( requests.get(0), "101", "B", true);
        verifyRequestStatus( requests.get(1), "102", "A", false);
        verifyRequestStatus( requests.get(2), "103", "A", false);
    }
    /**
     * 添付ファイル複数有りパターンの電子署名確認。
     */
    @Test
    public void testSmimeMultiAttachedFile() throws Exception {

        FreeTextMailContext mailContext = new FreeTextMailContext();
        mailContext.setSubject("けんめい");
        mailContext.setMailBody("本文");
        mailContext.setCharset("utf-8");
        mailContext.setFrom("from@from.com");
        mailContext.addTo("to1@localhost");
        mailContext.addCc("cc1@localhost");
        mailContext.addBcc("bcc1@localhost");
        mailContext.setMailSendPatternId("04");
        mailContext.addAttachedFile(new AttachedFile("text/plain", new File("src/test/resources/please/change/me/common/mail/smime/data/temp1.txt")));
        mailContext.addAttachedFile(new AttachedFile("application/pdf", new File("src/test/resources/please/change/me/common/mail/smime/data/temp1.pdf")));
        mailRequest(mailContext);
        // 処理対象外
        mailContext.setMailSendPatternId("00");
        mailRequest(mailContext);
        // 処理対象外
        mailContext.setMailSendPatternId("02");
        mailRequest(mailContext);

        // バッチ実行
        CommandLine commandLine = new CommandLine("-diConfig",
                "please/change/me/common/mail/smime/SmimeSignedMailSenderTest.xml", "-requestPath",
                "please.change.me.common.mail.smime.SMIMESignedMailSender/SENDMAIL00", "-userId", "userid",
                "-mailSendPatternId", "04");
        int exitCode = Main.execute(commandLine);

        assertThat(exitCode, is(0));

        // ログアサート
        assertLog("メール送信要求が 1 件あります。");
        assertLog("メール送信完了。メールリクエストID 101");

        // 送信したメッセージの確認
        Message[] toMessages = getMailMessage("to1");
        Message[] ccMessages = getMailMessage("cc1");
        Message[] bccMessages = getMailMessage("bcc1");

        // メッセージが1つう送信されていること
        assertThat(toMessages, is(notNullValue()));
        assertThat(toMessages.length, is(1));
        assertThat(ccMessages, is(notNullValue()));
        assertThat(ccMessages.length, is(1));
        assertThat(bccMessages, is(notNullValue()));
        assertThat(bccMessages.length, is(1));

        // 各メッセージ部をアサート
        for (Message message : new Message[]{toMessages[0], ccMessages[0], bccMessages[0]}) {

            // メッセージ共通部のアサート
            assertThat(message.getContentType(), is(containsString("multipart/signed;")));
            assertThat(message.getContentType(), is(containsString("application/pkcs7-signature")));
            assertThat(message.getContentType(), is(containsString("micalg=sha-1;")));
            assertThat(message.getFrom().length, is(1));
            assertThat((InternetAddress) message.getFrom()[0], is(new InternetAddress("from@from.com")));

            assertThat(message.getContent(), is(instanceOf(Multipart.class)));
            assertThat(message.getSubject(), is("けんめい"));

            //----------------------------------------------------------------------
            // 本文部分のアサート
            //----------------------------------------------------------------------
            Multipart multipart = (Multipart) message.getContent();
            assertThat(multipart.getCount(), is(2));
            BodyPart body = multipart.getBodyPart(0);
            assertThat(body.getContentType(), is(containsString("multipart/mixed")));
            assertThat(body.getContent(), is(instanceOf(Multipart.class)));
            Multipart bodyPart = (Multipart) body.getContent();

            // 本文部は、本文と添付ファイル2ファイルの3つ
            assertThat(bodyPart.getCount(), is(3));
            BodyPart bodyText = bodyPart.getBodyPart(0);
            assertThat(bodyText.getContentType(), is(containsString("text/plain")));
            assertThat(bodyText.getContentType(), is(containsString("charset=utf-8")));
            assertThat((String) bodyText.getContent(), is("本文"));

            BodyPart file = bodyPart.getBodyPart(1);
            assertThat(file.getFileName(), is("temp1.txt"));
            assertThat(file.getContentType(), is(containsString("text/plain")));
            assertThat((String) file.getContent(), is("l-1\r\nl-2\r\n"));

            BodyPart pdf = bodyPart.getBodyPart(2);
            assertThat(pdf.getFileName(), is("temp1.pdf"));
            assertThat(pdf.getContentType(), is(containsString("application/pdf")));

            //----------------------------------------------------------------------
            // 電子署名部のアサート
            //----------------------------------------------------------------------
            BodyPart smime = multipart.getBodyPart(1);
            assertThat(smime.getFileName(), is("smime.p7s"));
        }

        List<MailSendRequest> requests = VariousDbTestHelper.findAll(MailSendRequest.class, "mailRequestId");
        assertThat("リクエスト数が一致すること", requests.size(), is(3));
        verifyRequestStatus( requests.get(0), "101", "B", true);
        verifyRequestStatus( requests.get(1), "102", "A", false);
        verifyRequestStatus( requests.get(2), "103", "A", false);
    }

    /**
     * 添付ファイルのcontentTypeを空文字列にした場合。
     *
     * 電子署名生成でエラーになること。
     */
    @Test
    public void testAttachedFileContextTypeNull() throws Exception {

        FreeTextMailContext mailContext = new FreeTextMailContext();
        mailContext.setSubject("けんめい");
        mailContext.setMailBody("本文");
        mailContext.setCharset("utf-8");
        mailContext.setFrom("from@from.com");
        mailContext.addTo("to1@localhost");
        mailContext.addCc("cc1@localhost");
        mailContext.addBcc("bcc1@localhost");
        mailContext.setMailSendPatternId("01");
        mailContext.addAttachedFile(new AttachedFile("", new File("src/test/resources/please/change/me/common/mail/smime/data/temp1.txt")));
        mailRequest(mailContext);
        // 処理対象外
        mailContext.setMailSendPatternId("00");
        mailRequest(mailContext);
        // 処理対象外
        mailContext.setMailSendPatternId("02");
        mailRequest(mailContext);

        // バッチ実行
        CommandLine commandLine = new CommandLine("-diConfig",
                "please/change/me/common/mail/smime/SmimeSignedMailSenderTest.xml", "-requestPath",
                "please.change.me.common.mail.smime.SMIMESignedMailSender/SENDMAIL00", "-userId", "userid",
                "-mailSendPatternId", "02");
        int exitCode = Main.execute(commandLine);
        assertThat("電子署名に失敗なので異常終了。",exitCode, is(199));

        // ログアサート
        assertLog("メール送信要求が 1 件あります。");
        assertLog("[199 ProcessAbnormalEnd] メール送信失敗：メールリクエストID 103");

        List<MailSendRequest> requests = VariousDbTestHelper.findAll(MailSendRequest.class, "mailRequestId");
        assertThat("リクエスト数が一致すること", requests.size(), is(3));
        verifyRequestStatus( requests.get(0), "101", "A", false);
        verifyRequestStatus( requests.get(1), "102", "A", false);
        verifyRequestStatus( requests.get(2), "103", "Z", false);
    }

    /**
     * 添付ファイルのcontentTypeを空文字列にした場合。
     *
     * 電子署名生成でエラーになること。
     */
    @Test
    public void testNoCertificatePatternId() throws Exception {

        FreeTextMailContext mailContext1 = new FreeTextMailContext();
        mailContext1.setSubject("けんめい");
        mailContext1.setMailBody("本文");
        mailContext1.setCharset("utf-8");
        mailContext1.setFrom("from@from.com");
        mailContext1.addTo("to1@localhost");
        mailContext1.addCc("cc1@localhost");
        mailContext1.addBcc("bcc1@localhost");
        mailContext1.setMailSendPatternId("05");// CertificateWrapperが存在しないID
        mailRequest(mailContext1);
        FreeTextMailContext mailContext2 = new FreeTextMailContext();
        mailContext2.setSubject("けんめい2");
        mailContext2.setMailBody("本文2");
        mailContext2.setCharset("utf-8");
        mailContext2.setFrom("from@from.com");
        mailContext2.addTo("to1@localhost");
        mailContext2.addCc("cc1@localhost");
        mailContext2.addBcc("bcc1@localhost");
        mailContext2.setMailSendPatternId("05");// CertificateWrapperが存在しないID
        mailRequest(mailContext2);

        // バッチ実行
        CommandLine commandLine = new CommandLine("-diConfig",
                "please/change/me/common/mail/smime/SmimeSignedMailSenderResidentTest.xml", "-requestPath",
                "please.change.me.common.mail.smime.SMIMESignedMailSender/SENDMAIL00", "-userId", "userid",
                "-mailSendPatternId", "05");
        int exitCode = Main.execute(commandLine);

        assertThat("設定不備なので1通目でプロセスは異常終了する。", exitCode, is(199));

        // ログアサート
        assertLog("メール送信要求が 2 件あります。");
        assertLog("No certification setting. mailSendPatternId=[05]");
        assertLog("[199 ProcessAbnormalEnd] メール送信失敗：メールリクエストID 101");

        List<MailSendRequest> requests = VariousDbTestHelper.findAll(MailSendRequest.class, "mailRequestId");
        assertThat("リクエスト数が一致すること", requests.size(), is(2));
        verifyRequestStatus( requests.get(0), "101", "Z", false);
        verifyRequestStatus( requests.get(1), "102", "A", false);
    }

    /**
     * メール送信要求のステータスを検証する。
     *
     * @param request   メール送信要求
     * @param requestId ID
     * @param status    送信ステータス
     * @param isNotNullSendTimestamp    送信日時がNotNullかどうか
     */
    private void verifyRequestStatus(final MailSendRequest request, final String requestId, final String status, final boolean isNotNullSendTimestamp) {
        assertThat("リクエストID", request.mailRequestId, is(requestId));
        assertThat("送信ステータス", request.status, is(status));
        if(isNotNullSendTimestamp) {
            assertThat("送信日時が登録済み", request.sendingTimestamp, notNullValue());
        } else {
            assertThat("送信日時が未登録", request.sendingTimestamp, nullValue());
        }
    }
}


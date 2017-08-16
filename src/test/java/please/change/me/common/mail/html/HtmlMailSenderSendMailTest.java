package please.change.me.common.mail.html;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage.RecipientType;

import nablarch.common.mail.FreeTextMailContext;
import nablarch.common.mail.MailConfig;
import nablarch.core.db.statement.SqlResultSet;
import nablarch.core.db.statement.SqlRow;
import nablarch.core.db.transaction.SimpleDbTransactionManager;
import nablarch.core.repository.SystemRepository;
import nablarch.fw.launcher.CommandLine;
import nablarch.fw.launcher.Main;
import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.db.helper.DatabaseTestRunner;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * {@link HtmlMailSender}のテスト。<br />
 * 実際にメールを送信してテストする。<br />
 *
 * メールを送信しない単体テストは{@link HtmlMailSenderTest}にて実施する。
 *
 * <p>
 *  内臓サーバーを利用していないため、送信に利用するメールサーバーがローカル環境に設定されていることが前提となる。<br />
 *  MailAccountManagerで設定するアカウントを設定すること。
 * </p>
 *
 * James 2系であれば
 * <ol>
 *   <li>アーカイブを取得、任意のディレクトリに展開する。(JAMES_HOME)</li>
 *   <li>%JAMES_HOME%/bin/run.batを実行する。</li>
 *   <li>SMTP,POP3のポートの設定がMailSessionやテスト内の設定とあっているか確認する。</li>
 *   <li>telnet localhost %MANAGE_PORT% で管理コンソールから必要なユーザーを登録する。</li>
 *   <li>テストを実行する。</li>
 * </ol>
 *
 * その他のメールサーバーを利用する場合は、利用方法を確認すること。
 *
 * @author tani takanori
 */
@RunWith(DatabaseTestRunner.class)
public class HtmlMailSenderSendMailTest {

    private static HtmlMailTestDbSupport db = new HtmlMailTestDbSupport("mailtest");

    private MailConfig mailConfig;

    private SimpleDbTransactionManager transactionManager;

    @Rule
    public final SystemRepositoryResource resource = new SystemRepositoryResource(
            "please/change/me/common/mail/html/mailSenderTest.xml");

    private static final String REPLY_TO = "replyto@localhost";
    private static final String RETURN_PATH = "returnPath@localhost";
    private static final String CHARSET = "UTF-8";
    private static final String FROM = "form@localhost";

    /**
     * テストで利用するメールアカウント。<br />
     *
     * メールサーバーを利用するテストのため、このクラスで定義しているアカウントを作成する必要がある。<br />
     * 実際にメールを送信するために、ここに設定されたアカウントのアドレスを変更する場合、
     * {@link #cleanupMail(String)}を実行しないようにすること。
     *
     * @author tani takanori
     */
    private static class MailAccountManager {
        private static final String TO = "to1@localhost";
        private static final String CC = "cc1@localhost";
        private static final String BCC = "bcc1@localhost";
        private static final String PASSWORD = "default";
        @SuppressWarnings("serial")
        private static final Properties props = new Properties() {{
            setProperty("mail.smtp.host", getEnv("mail.smtp.host", "localhost"));
            setProperty("mail.host",      getEnv("mail.host", "localhost"));
            setProperty("mail.pop3.host", getEnv("mail.pop3.host", "localhost"));
            setProperty("mail.pop3.port", getEnv("mail.pop3.port", "10110"));
        }};

        /**
         * 環境情報を取得する。
         *
         * @param key キー
         * @param defaultVal 環境情報にキーが存在しない場合のデフォルト値
         * @return キーで取得された環境情報
         */
        private static String getEnv(String key, String defaultVal) {
            String val = SystemRepository.getString(key);
            return val != null ? val : defaultVal;
        }

        /**
         * アカウントの{@link Store}を取得する。
         *
         * @param account アカウント
         * @param debug デバッグするかどうか
         * @return アカウントのStore
         * @throws NoSuchProviderException POP3が利用できない場合
         */
        private static Store getStoreFor(final String account, boolean debug) throws NoSuchProviderException {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(account.split("@")[0], PASSWORD);
                }
            });
            session.setDebug(debug);
            return session.getStore("pop3");
        }

        /**
         * メールをクリーンする。
         *
         * @throws Exception 例外
         */
        private static void cleanup() throws Exception {
            for (String account : getAllAccounts()) {
                cleanupMail(account);
            }
        }

        private static String[] getAllAccounts() {
            return new String[] {TO, CC, BCC};
        }

        /**
         * メールボックスを初期化する。
         *
         * @param account 対象のアカウント
         * @throws Exception 例外。
         */
        private static void cleanupMail(final String account) throws Exception {
            // 初期化時にデバッグすると邪魔なので、debugはfalse.
            Store store = MailAccountManager.getStoreFor(account, false);
            store.connect();
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_WRITE);
            Message[] messages = folder.getMessages();

            System.out.println("account " + account + ": " + messages.length
                    + " messages will be deleted.");

            for (int i = 0; i < messages.length; i++) {
                messages[i].setFlag(Flags.Flag.DELETED, true);
            }
            folder.close(true);
            store.close();
        }
    }

    /**
     * テーブルの初期化を行う。
     *
     * @throws Exception 想定外の例外
     */
    @BeforeClass
    public static void setupClass() throws Exception {
        HtmlMailTestDbSupport.initDB();
        db.deleteMessage();
        db.insertMessage("SEND_FAIL0", "メール送信に失敗しました。 mailRequestId=[{0}]", "send mail failed. mailRequestId=[{0}]");
        db.insertMessage("SEND_OK000", "メールを送信しました。 mailRequestId=[{0}]", "send mail. mailRequestId=[{0}]");
        db.insertMessage("REQ_COUNT0", "メール送信要求が {0} 件あります。", "{0} records of mail request selected.");
    }

    /**
     * テーブルの削除を行う。
     */
    @AfterClass
    public static void tearDownClass() {
        HtmlMailTestDbSupport.clearDb();
    }

    /**
     * 要求テーブルのレコードを削除する。
     *
     * @throws Exception Sql実行時の例外
     */
    @Before
    public void setDB() throws Exception {
        transactionManager = SystemRepository.get("dbManager-default");
        transactionManager.beginTransaction();
        db.deleteRequest();
        mailConfig = SystemRepository.get("mailConfig");
        MailAccountManager.cleanup();
    }

    private Folder openFolder(Store store) throws Exception {
        Folder folder = null;
        for (int i =0; i < 20; i++) {
            folder = store.getFolder("INBOX");
            folder.open(Folder.READ_WRITE);
            Thread.sleep(3000);
            if (folder.getMessageCount() > 0) {
                break;
            }
            folder.close(true);
        }
        return folder;
    }

    /**
     * テスト後の処理
     */
    @After
    public void tearDown() {
        transactionManager.endTransaction();
    }

    /**
     * コミットする。
     *
     */
    private void commit() {
        transactionManager.commitTransaction();
    }

    /**
     * プレーンテキストのメールを送信する。
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void sendPlainTextMail() throws Exception {
        FreeTextMailContext request = new FreeTextMailContext();
        request.setSubject("PlainSubject");
        request.setMailBody("プレーンテキストメール");
        request.setFrom(FROM);
        request.setCharset(CHARSET);
        request.setReplyTo(REPLY_TO);
        request.setReturnPath(RETURN_PATH);
        request.addTo(MailAccountManager.TO);
        db.insertReqeustToSend(request, "1");
        commit();
        SqlResultSet requestDataList = db.findMailRequest();
        assertThat(requestDataList.size(), is(1));
        SqlRow requestData = requestDataList.get(0);
        assertThat("送信日時は空", requestData.get("sendDatetime"), nullValue());

        // バッチ実行
        CommandLine commandLine = new CommandLine(
                "-diConfig", "please/change/me/common/mail/html/mailSenderTest.xml",
                "-requestPath", "please.change.me.common.mail.html.HtmlMailSender/SENDMAIL00",
                "-userId", "unused");
        int execute = Main.execute(commandLine);

        // 実行結果の検証
        assertThat("正常終了なので戻り値は0となる。", execute, is(0));
        requestDataList = db.findMailRequest();
        assertThat(requestDataList.size(), is(1));
        requestData = requestDataList.get(0);
        assertThat(requestData.getString("status"), is(mailConfig.getStatusSent()));
        assertThat("送信日時が登録されているはず", requestData.get("sendDatetime"), notNullValue());

        Store store = MailAccountManager.getStoreFor(MailAccountManager.TO, true);
        store.connect();
        Folder folder = openFolder(store);
        Message[] messages = folder.getMessages();
        assertThat("メールが1通とどいているはず", messages.length, is(1));
        Message message = messages[0];
        Address[] messageFrom = message.getFrom();
        assertThat("fromアドレスの数", messageFrom.length, is(1));
        assertThat("fromアドレス", ((InternetAddress) messageFrom[0]).getAddress(), is(FROM));

        Address[] messageTO = message.getRecipients(RecipientType.TO);
        assertThat("TOアドレス", messageTO.length, is(1));
        assertThat(((InternetAddress)messageTO[0]).getAddress(), is(MailAccountManager.TO));

        Address[] messageCC = message.getRecipients(RecipientType.CC);
        assertThat("CCアドレス", messageCC, is(nullValue()));

        Address[] messageReplyTo = message.getReplyTo();
        assertThat("ReplyToの数", messageReplyTo.length, is(1));
        assertThat("ReplyToアドレス", ((InternetAddress) messageReplyTo[0]).getAddress(), is(REPLY_TO));

        String[] messageReturnPath = message.getHeader("Return-Path");
        assertThat("RetrunPathの数", messageReturnPath.length, is(1));
        assertThat("RetrunPathアドレス", messageReturnPath[0], is("<" + RETURN_PATH + ">"));

        assertThat("Content-Type(text/plainであること)", message.getContentType(), containsString("text/plain"));
        assertThat("Content-Type(text/htmlがふくまれない)", message.getContentType(), not((containsString("text/html"))));
        assertThat("charset", message.getContentType(), containsString(CHARSET));

        assertThat("コンテンツはString型", message.getContent(), is(instanceOf(String.class)));
        assertThat("本文", (String) message.getContent(), is("プレーンテキストメール"));
    }

    /**
     * HTMLメールを送信する。(添付ファイルなし)
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void testSendHtmlMail() throws Exception {
        FreeTextMailContext request = new FreeTextMailContext();
        String mailBody = "<html><head><meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\"/><style>h1{color:#DDFFEE;}</style></head><body><h1>伊藤さんへのHTMLテキストでのテストです。</h1></body></html>";
        request.setSubject("HTML");
        request.setMailBody(mailBody);
        request.setFrom(FROM);
        request.setCharset(CHARSET);
        request.setReplyTo(REPLY_TO);
        request.setReturnPath(RETURN_PATH);
        request.addTo(MailAccountManager.TO);
        db.insertReqeustToSend(request, "1");
        db.insertHtmlRequest("1", "代替テキスト");
        commit();

        SqlResultSet requestDataList = db.findMailRequest();
        assertThat(requestDataList.size(), is(1));
        SqlRow requestData = requestDataList.get(0);
        assertThat("送信日時は空", requestData.get("sendDatetime"), nullValue());
        SqlResultSet htmlRequests = db.findHtmlMailRequest();
        assertThat("HTMLメールの要求はある。", htmlRequests.size(), is(1));
        assertThat("メール送信要求に紐づいている。", htmlRequests.get(0).getString("mailRequestId"), is("1"));

        // バッチ実行
        CommandLine commandLine = new CommandLine(
                "-diConfig", "please/change/me/common/mail/html/mailSenderTest.xml",
                "-requestPath", "please.change.me.common.mail.html.HtmlMailSender/SENDMAIL00",
                "-userId", "unused");
        int execute = Main.execute(commandLine);

        // 実行結果の検証
        assertThat("正常終了なので戻り値は0となる。", execute, is(0));
        requestDataList = db.findMailRequest();
        assertThat(requestDataList.size(), is(1));
        requestData = requestDataList.get(0);
        assertThat(requestData.getString("status"), is(mailConfig.getStatusSent()));
        assertThat("送信日時が登録されているはず", requestData.get("sendDatetime"), notNullValue());

        Store store = MailAccountManager.getStoreFor(MailAccountManager.TO, true);
        store.connect();
        Folder folder = openFolder(store);
        Message[] messages = folder.getMessages();
        assertThat("メールが1通とどいているはず", messages.length, is(1));
        Message message = messages[0];
        Address[] messageFrom = message.getFrom();
        assertThat("fromアドレスの数", messageFrom.length, is(1));
        assertThat("fromアドレス", ((InternetAddress) messageFrom[0]).getAddress(), is(FROM));

        Address[] messageTO = message.getRecipients(RecipientType.TO);
        assertThat("TOアドレス", messageTO.length, is(1));
        assertThat(((InternetAddress)messageTO[0]).getAddress(), is(MailAccountManager.TO));

        Address[] messageCC = message.getRecipients(RecipientType.CC);
        assertThat("CCアドレス", messageCC, is(nullValue()));

        Address[] messageReplyTo = message.getReplyTo();
        assertThat("ReplyToの数", messageReplyTo.length, is(1));
        assertThat("ReplyToアドレス", ((InternetAddress) messageReplyTo[0]).getAddress(), is(REPLY_TO));

        String[] messageReturnPath = message.getHeader("Return-Path");
        assertThat("RetrunPathの数", messageReturnPath.length, is(1));
        assertThat("RetrunPathアドレス", messageReturnPath[0], is("<" + RETURN_PATH + ">"));

        assertThat("Content-Type", message.getContentType(), containsString("multipart/alternative"));
        assertThat("コンテンツはMultiPart型", message.getContent(), is(instanceOf(Multipart.class)));
        Multipart textpart = (Multipart) message.getContent();

        // 代替テキスト
        assertThat("はじめは代替テキスト", textpart.getBodyPart(0).getContent(), is(instanceOf(String.class)));
        assertThat("はじめは代替テキスト", textpart.getBodyPart(0).getContentType(), containsString("text/plain"));
        String body = (String) textpart.getBodyPart(0).getContent();
        assertThat("はじめは代替テキスト", body, is("代替テキスト"));

        // HTML
        assertThat("HTMLは後", textpart.getBodyPart(1).getContent(), is(instanceOf(String.class)));
        assertThat("HTMLは後", textpart.getBodyPart(1).getContentType(), containsString("text/html"));
        body = (String) textpart.getBodyPart(1).getContent();
        assertThat("HTMLは後", body, is(mailBody));
    }
}

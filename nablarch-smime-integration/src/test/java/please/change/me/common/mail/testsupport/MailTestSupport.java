package please.change.me.common.mail.testsupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import please.change.me.common.mail.testsupport.entity.IdGenerator;
import please.change.me.common.mail.testsupport.entity.MailAttachedFile;
import please.change.me.common.mail.testsupport.entity.MailMessage;
import please.change.me.common.mail.testsupport.entity.MailRecipient;
import please.change.me.common.mail.testsupport.entity.MailSendRequest;
import please.change.me.common.mail.testsupport.entity.MailTemplate;

import nablarch.common.mail.FreeTextMailContext;
import nablarch.common.mail.MailRequester;
import nablarch.common.mail.MailUtil;
import nablarch.common.mail.TemplateMailContext;
import nablarch.core.db.connection.AppDbConnection;
import nablarch.core.db.connection.ConnectionFactory;
import nablarch.core.db.transaction.SimpleDbTransactionExecutor;
import nablarch.core.db.transaction.SimpleDbTransactionManager;
import nablarch.core.repository.SystemRepository;
import nablarch.core.transaction.TransactionFactory;
import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.db.helper.VariousDbTestHelper;
import nablarch.test.support.log.app.OnMemoryLogWriter;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;

/**
 * メール関連のテストをサポートするクラス。
 *
 * @author hisaaki sioiri
 */
public class MailTestSupport {

    @ClassRule
    public static final SystemRepositoryResource RESOURCE = new SystemRepositoryResource(
      "please/change/me/common/mail/smime/SmimeSignedMailSenderTest.xml"
    );

    @BeforeClass
    public static void setupClass() {
        // テーブルの作成
        VariousDbTestHelper.createTable(MailSendRequest.class);
        VariousDbTestHelper.createTable(MailRecipient.class);
        VariousDbTestHelper.createTable(MailAttachedFile.class);
        VariousDbTestHelper.createTable(MailTemplate.class);
        VariousDbTestHelper.createTable(IdGenerator.class);
        VariousDbTestHelper.createTable(MailMessage.class);
    }

    @Before
    public void setup() throws Exception {

        OnMemoryLogWriter.clear();

        cleaningTable(
                MailSendRequest.class,
                MailRecipient.class,
                MailAttachedFile.class,
                MailTemplate.class,
                MailMessage.class,
                IdGenerator.class);

        // メールテンプレートデータの準備
        VariousDbTestHelper.insert(
                new MailTemplate("001", "JP", "タイトル",
                        "本文\n{line2}\n{line3}", "utf-8"));
        VariousDbTestHelper.insert(
                new MailTemplate("001", "EN", "title",
                "body\n{line2}\n{line3}", "utf-8"));
        VariousDbTestHelper.insert(
                new MailTemplate("002", "JP", "タイトル。",
                        "ほんぶん。", null));
        VariousDbTestHelper.insert(
                new MailTemplate("003", "JP", "タイトル。",
                        "ほんぶん。", null));

        // 採番テーブルの初期データ準備
        VariousDbTestHelper.insert(new IdGenerator("99",100));

        // メッセージの登録
        VariousDbTestHelper.insert(new MailMessage("001","ja",
                "メール送信要求が {0} 件あります。"));
        VariousDbTestHelper.insert(new MailMessage("002","ja",
                "メール送信完了。メールリクエストID {0}"));
        VariousDbTestHelper.insert(new MailMessage("ZZZ","en",
                "メール送信失敗：メールリクエストID {0}"));
        VariousDbTestHelper.insert(new MailMessage("ZZZ","ja",
                "メール送信失敗：メールリクエストID {0}"));
    }

    /**
     * 指定されたクラスのデータを削除する。
     *
     * @param classes クラス一覧
     */
    private void cleaningTable(Class... classes) {
        for (Class clazz : classes) {
            VariousDbTestHelper.delete(clazz);
        }
    }

    @AfterClass
    public static void afterClass() {
        VariousDbTestHelper.dropTable(MailSendRequest.class);
        VariousDbTestHelper.dropTable(MailRecipient.class);
        VariousDbTestHelper.dropTable(MailAttachedFile.class);
        VariousDbTestHelper.dropTable(MailTemplate.class);
        VariousDbTestHelper.dropTable(IdGenerator.class);
        VariousDbTestHelper.dropTable(MailMessage.class);
    }

    protected void mailRequest(final FreeTextMailContext mailContext) {
        SimpleDbTransactionManager manager = new SimpleDbTransactionManager();
        ConnectionFactory connectionFactory = SystemRepository.get("connectionFactory");
        TransactionFactory transactionFactory = SystemRepository.get("jdbcTransactionFactory");
        manager.setConnectionFactory(connectionFactory);
        manager.setTransactionFactory(transactionFactory);
        new SimpleDbTransactionExecutor<Void>(manager) {
            @Override
            public Void execute(AppDbConnection connection) {
                MailRequester mailRequester = MailUtil.getMailRequester();
                mailRequester.requestToSend(mailContext);
                return null;
            }
        }.doTransaction();
    }

    protected void mailRequest(final TemplateMailContext mailContext) {
        SimpleDbTransactionManager manager = new SimpleDbTransactionManager();
        ConnectionFactory connectionFactory = SystemRepository.get("connectionFactory");
        TransactionFactory transactionFactory = SystemRepository.get("jdbcTransactionFactory");
        manager.setConnectionFactory(connectionFactory);
        manager.setTransactionFactory(transactionFactory);
        new SimpleDbTransactionExecutor<Void>(manager) {
            @Override
            public Void execute(AppDbConnection connection) {
                MailRequester mailRequester = MailUtil.getMailRequester();
                mailRequester.requestToSend(mailContext);
                return null;
            }
        }.doTransaction();
    }

    protected static void assertLog(String message) {
        List<String> log = OnMemoryLogWriter.getMessages("writer.mail");
        System.out.println("log = " + log);
        boolean writeLog = false;
        for (String logMessage : log) {
            String str = logMessage.replaceAll("\\r|\\n", "");
            if (str.indexOf(message) >= 0) {
                writeLog = true;
            }
        }
        assertThat("ログが出力されていること", writeLog, is(true));
    }
}




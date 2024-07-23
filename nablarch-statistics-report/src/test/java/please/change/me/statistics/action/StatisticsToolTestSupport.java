package please.change.me.statistics.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import nablarch.core.db.connection.AppDbConnection;
import nablarch.core.db.statement.SqlPStatement;
import nablarch.core.db.statement.exception.SqlStatementException;
import nablarch.core.db.transaction.SimpleDbTransactionExecutor;
import nablarch.core.db.transaction.SimpleDbTransactionManager;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.ComponentDefinitionLoader;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.util.FileUtil;
import nablarch.core.util.ObjectUtil;
import nablarch.fw.launcher.CommandLine;
import nablarch.fw.launcher.Main;
import please.change.me.statistics.MemoryLogWriter;

/**
 * 統計情報ツールの自動テストサポートクラス。
 *
 * @author hisaaki sioiri
 */
public class StatisticsToolTestSupport {

    /** システムプロパティのバックアップ */
    private Properties systemProperties;

    /**
     * テスト開始時の処理。
     * <p/>
     * リポジトリの初期化を行う。
     */
    @BeforeClass
    public static void setupClass() {
        ComponentDefinitionLoader loader = new XmlComponentDefinitionLoader("unit-test.xml");
        SystemRepository.load(new DiContainer(loader));
    }

    /** テスト終了時の処理。 */
    @AfterClass
    public static void afterClass() {
        SystemRepository.clear();
    }

    /**
     * ターゲットクラスのバッチアクションを実行する。
     *
     * @param actionClassName 実行対象のバッチアクションクラス名
     * @return バッチアクションから返された終了コード
     */
    protected static int executeBatchAction(String actionClassName) {
        CommandLine commandLine = new CommandLine(
                "-diConfig", "statistics-batch-for-test.xml",
                "-requestPath", actionClassName ,
                "-userId", "statistics-user");
        return Main.execute(commandLine);
    }

    /** テストの準備処理 */
    @Before
    public void setup() {
        MemoryLogWriter.outputs.clear();
        systemProperties = new Properties();
        systemProperties.putAll(System.getProperties());
        cleaningTestDir(
                "test/temp/online/log/online-parse-output",
                "test/temp/online/log/online-temp",
                "test/temp/online/summary/output"
        );
    }

    @After
    public void after() {
        System.setProperties(systemProperties);
    }

    /**
     * テストで使用するディレクトリにクリーニング処理。
     *
     * @param dirs クリーニング対象のディレクトリリスト
     */
    private void cleaningTestDir(String... dirs) {
        for (String dir : dirs) {
            File[] files = FileUtil.listFiles(dir, "*");
            if (files == null) {
                continue;
            }
            for (File file : files) {
                FileUtil.deleteFile(file);
            }
        }
    }

    /**
     * ファイルの内容が同じかどうかを比較するMatcherを返す。
     *
     * @param expected 期待値
     * @return ファイル内用を比較するMatcher
     */
    protected TypeSafeMatcher<File> sameFile(final String expected) {
        return new TypeSafeMatcher<File>() {
            private int lineNo;

            @Override
            protected boolean matchesSafely(File file) {
                try {
                    BufferedReader actualReader = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                            Charset.forName("ms932")));
                    BufferedReader expectedReader = new BufferedReader(
                            new InputStreamReader(FileUtil.getClasspathResource(expected), Charset.forName("utf-8")));

                    String actualLine;
                    while ((actualLine = actualReader.readLine()) != null) {
                        lineNo++;
                        String expectedLine = expectedReader.readLine();
                        if (!actualLine.equals(expectedLine)) {
                            System.out.println("actualLine = " + actualLine);
                            System.out.println("expectedLine = " + expectedLine);
                            return false;
                        }
                    }
                    // 実行結果を読み終わった後に、期待値が存在していた場合は不一致
                    if (expectedReader.readLine() != null) {
                        lineNo++;
                        return false;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(expected + ":record no = " + lineNo);
            }
        };
    }
}


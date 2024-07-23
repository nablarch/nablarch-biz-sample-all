package please.change.me.statistics.action;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;

import please.change.me.statistics.MemoryLogWriter;

import nablarch.core.util.FileUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@link OnlineAccessLogParseAction}のテストクラス。
 *
 * @author hisaaki sioiri
 */
public class OnlineAccessLogParseActionTest extends StatisticsToolTestSupport {

    /** ターゲットクラスのクラス名 */
    private static final String ACTION_CLASS_NAME = "OnlineAccessLogParseAction";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private String tempDir;

    private String onlineParseOutputDir;

    @Before
    public void setUp() throws Exception {
        tempDir = temporaryFolder.newFolder("temp")
                                 .getAbsolutePath();

        onlineParseOutputDir = temporaryFolder.newFolder("online-parse-output")
                                              .getAbsolutePath();
        System.setProperty("access-log.parse.temp.dir", tempDir);
        System.setProperty("request-info.dir", "file:" + onlineParseOutputDir);
    }

    /**
     * 正常系のテスト。
     * <p/>
     * アクセスログが正常に解析され、リクエスト情報中間ファイルが出力されることを確認する。
     */
    @Test
    public void testNormalEnd() {
        // ターゲットの実行
        int exitCode = executeBatchAction(ACTION_CLASS_NAME);
        assertThat(exitCode, is(0));

        // 作業ディレクトリからはファイルが削除されていること。
        File[] files = FileUtil.listFiles(tempDir, "^[^.]*");
        assertThat(files.length, is(0));

        // リクエスト情報ファイルのアサート
        File[] requestInfoFiles = FileUtil.listFiles(onlineParseOutputDir, "^[^.]*");
        assertThat(requestInfoFiles.length, is(1));
        assertThat(requestInfoFiles[0].getName(), is("REQUEST_INFO_20120910.csv"));

        // ファイル内容のアサート
        assertThat(requestInfoFiles[0], is(sameFile(
                "please/change/me/statistics/action/expected/OnlineAccessLogParseActionTest-expected1.csv")));

    }

    /**
     * 終了ログからプロセス名が抽出出来ない場合。
     * <p/>
     * プロセス名部分が空で解析結果が出力される。
     */
    @Test
    public void testProcessNameNotFound() {
        System.setProperty("access-log.name.pattern", "processNameError.log");

        int exitCode = executeBatchAction("OnlineAccessLogParseAction");
        assertThat(exitCode, is(0));

        File[] requestInfoFiles = FileUtil.listFiles(onlineParseOutputDir, "^[^.]*");
        assertThat(requestInfoFiles.length, is(1));
        assertThat(requestInfoFiles[0].getName(), is("REQUEST_INFO_20120910.csv"));

        // ファイル内容のアサート
        assertThat(requestInfoFiles[0], is(sameFile(
                "please/change/me/statistics/action/expected/OnlineAccessLogParseActionTest-expected2.csv")));
    }

    /**
     * 終了ログからリクエストIDが抽出出来ない場合。
     * <p/>
     * 異常終了するので、終了コードは20となる。
     */
    @Test
    public void testRequestIdNotFound() {
        System.setProperty("access-log.dir", "src/test/temp/online/log/error-input");
        System.setProperty("access-log.name.pattern", "requestIdError.log");

        int exitCode = executeBatchAction("OnlineAccessLogParseAction");
        assertThat(exitCode, is(20));

        assertThat(MemoryLogWriter.outputs.size(), is(1));

        assertThat(MemoryLogWriter.outputs.get(0), is(containsString("LogParseException")));
        assertThat(MemoryLogWriter.outputs.get(0), is(containsString(
                "online access log parse error. REQUEST_ID was not found in end log.")));
    }

    /**
     * 終了ログからステータスコードが抽出出来ない場合。
     * <p/>
     * 異常終了するので、終了コードは20となる。
     */
    @Test
    public void testStatusCodeNotFound() {
        System.setProperty("access-log.dir", "src/test/temp/online/log/error-input");
        System.setProperty("access-log.name.pattern", "statusCodeError.log");

        int exitCode = executeBatchAction("OnlineAccessLogParseAction");
        assertThat(exitCode, is(20));

        assertThat(MemoryLogWriter.outputs.size(), is(1));

        assertThat(MemoryLogWriter.outputs.get(0), is(containsString("LogParseException")));
        assertThat(MemoryLogWriter.outputs.get(0), is(containsString(
                "online access log parse error. STATUS_CODE was not found in end log.")));
    }

    /**
     * 終了ログから処理時間が抽出出来ない場合。
     * <p/>
     * 異常終了するので、終了コードは20となる。
     */
    @Test
    public void testExecutionTimeNotFound() {
        System.setProperty("access-log.dir", "src/test/temp/online/log/error-input");
        System.setProperty("access-log.name.pattern", "executionTimeError.log");

        int exitCode = executeBatchAction("OnlineAccessLogParseAction");
        assertThat(exitCode, is(20));

        assertThat(MemoryLogWriter.outputs.size(), is(1));

        assertThat(MemoryLogWriter.outputs.get(0), is(containsString("LogParseException")));
        assertThat(MemoryLogWriter.outputs.get(0), is(containsString(
                "online access log parse error. EXECUTION_TIME was not found in end log.")));
    }

    /**
     * アクセスログファイルが空の場合
     * <p/>
     * 正常終了する。
     */
    @Test
    public void testEmptyAccessLog() {
        System.setProperty("access-log.dir", "src/test/temp/online/log/error-input");
        System.setProperty("access-log.name.pattern", "empty.log");

        int exitCode = executeBatchAction("OnlineAccessLogParseAction");
        assertThat(exitCode, is(0));

        assertThat(MemoryLogWriter.outputs.size(), is(0));

        File[] requestInfoFiles = FileUtil.listFiles(onlineParseOutputDir, "^[^.]*");
        assertThat(requestInfoFiles.length, is(1));
        assertThat(requestInfoFiles[0].getName(), is("REQUEST_INFO_20120910.csv"));

        // ファイル内容のアサート
        assertThat(requestInfoFiles[0].length(), is(0L));

    }
}


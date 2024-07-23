package please.change.me.statistics.action;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import nablarch.core.util.FileUtil;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link RequestInfoAggregateAction}のテストクラス
 *
 * @author hisaaki sioiri
 */
public class RequestInfoAggregateActionTest extends StatisticsToolTestSupport {
    
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    
    private String outputDir;

    @Before
    public void setUp() throws Exception {
        outputDir = temporaryFolder.newFolder("output").getAbsolutePath();
        System.setProperty("request-info-summary.dir", "file:" + outputDir);
    }

    /** 処理対象のリクエスト情報ファイルが1ファイルだけの場合 */
    @Test
    public void testSingleFile() {
        System.setProperty("request-info.dir", "file:src/test/temp/online/summary/input1");
        int exitCode = executeBatchAction("RequestInfoAggregateAction");
        assertThat(exitCode, is(0));

        // 年月ファイル
        File[] ymFiles11 = FileUtil.listFiles(outputDir, "REQUEST_INFO_SUMMARY_YM_201209_11.csv");
        assertThat(ymFiles11.length, is(1));
        assertThat(ymFiles11[0], is(sameFile(
                "please/change/me/statistics/action/expected/RequestInfoAggregateActionTest-ym1-11.csv")));

        File[] ymFiles12 = FileUtil.listFiles(outputDir, "REQUEST_INFO_SUMMARY_YM_201209_12.csv");
        assertThat(ymFiles12.length, is(1));
        assertThat(ymFiles12[0], is(sameFile(
                "please/change/me/statistics/action/expected/RequestInfoAggregateActionTest-ym1-12.csv")));


        // 日ファイル
        File[] dayFiles11 = FileUtil.listFiles(outputDir, "REQUEST_INFO_SUMMARY_DAY_11.csv");
        assertThat(dayFiles11.length, is(1));
        assertThat(dayFiles11[0], is(sameFile(
                "please/change/me/statistics/action/expected/RequestInfoAggregateActionTest-d1-11.csv")));

        File[] dayFiles12 = FileUtil.listFiles(outputDir, "REQUEST_INFO_SUMMARY_DAY_12.csv");
        assertThat(dayFiles12.length, is(1));
        assertThat(dayFiles12[0], is(sameFile(
                "please/change/me/statistics/action/expected/RequestInfoAggregateActionTest-d1-12.csv")));

        // 時間ファイル
        File[] hourFile11 = FileUtil.listFiles(outputDir, "REQUEST_INFO_SUMMARY_HOUR_11.csv");
        assertThat(hourFile11.length, is(1));
        assertThat(hourFile11[0], is(sameFile(
                "please/change/me/statistics/action/expected/RequestInfoAggregateActionTest-h1-11.csv")));

        File[] hourFile12 = FileUtil.listFiles(outputDir, "REQUEST_INFO_SUMMARY_HOUR_12.csv");
        assertThat(hourFile12.length, is(1));
        assertThat(hourFile12[0], is(sameFile(
                "please/change/me/statistics/action/expected/RequestInfoAggregateActionTest-h1-12.csv")));

    }

    /** 指定した集計期間内のファイルが集計対象となること。 */
    @Test
    public void testSummaryMonths() {

        System.setProperty("request-info.dir", "file:src/test/temp/online/summary/input2");
        System.setProperty("aggregate-period", "1");
        int exitCode = executeBatchAction("RequestInfoAggregateAction");
        assertThat(exitCode, is(0));

        File[] monthFiles = FileUtil.listFiles(outputDir, "REQUEST_INFO_SUMMARY_DAY_11.csv");
        assertThat(monthFiles.length, is(1));
        assertThat(monthFiles[0], is(sameFile(
                "please/change/me/statistics/action/expected/RequestInfoAggregateActionTest-d2.csv")));

    }
}


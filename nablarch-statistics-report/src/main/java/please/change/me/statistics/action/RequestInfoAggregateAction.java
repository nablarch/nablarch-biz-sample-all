package please.change.me.statistics.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import nablarch.core.util.DateUtil;
import nablarch.common.io.FileRecordWriterHolder;
import nablarch.core.dataformat.DataRecord;
import nablarch.core.date.SystemTimeUtil;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.FilePathSetting;
import nablarch.core.util.FileUtil;
import nablarch.core.util.StringUtil;
import nablarch.fw.DataReader;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Result;
import nablarch.fw.action.BatchAction;
import nablarch.fw.launcher.CommandLine;
import please.change.me.statistics.action.settings.OnlineStatisticsDefinition;
import please.change.me.statistics.reader.MultiFileRecordReader;

/**
 * {@link OnlineAccessLogParseAction}で出力したリクエスト情報CSVを集計するバッチアクション。
 * <p/>
 * 以下の集計ファイルを出力する。
 * <ul>
 * <li>
 * 年月集計結果CSVファイル<br/>
 * システム日付と同日のリクエスト情報ファイルから年月集計結果CSVファイルを出力する。(当月分の年月集計結果CSVファイルを出力する。)
 * </li>
 * <li>
 * 日付集計結果CSVファイル<br/>
 * {@link OnlineStatisticsDefinition#setAggregatePeriod(int)}で設定された期間分の日付集計結果CSVファイルを出力する。
 * </li>
 * <li>
 * 時間集計結果CSVファイル<br/>
 * {@link OnlineStatisticsDefinition#setAggregatePeriod(int)}で設定された期間分の時間集計結果CSVファイルを出力する。
 * </li>
 * </ul>
 * <p/>
 *
 * @author hisaaki sioiri
 */
public class RequestInfoAggregateAction extends BatchAction<DataRecord> {

    /** オンラインアクセスログ解析処理の設定値を{@link SystemRepository}から取得するためのキー値 */
    private static final String PARSE_DEFINITION_KEY = "onlineAccessLogParseDefinition";

    /** 日単位のリクエスト情報ファイルのファイル名のプレフィックス */
    private static final String REQUEST_INFO_SUMMARY_DAY_PREFIX = "REQUEST_INFO_SUMMARY_DAY_";

    /** 時間単位のリクエスト情報ファイルのファイル名のプレフィックス */
    private static final String REQUEST_INFO_SUMMARY_HOUR_PREFIX = "REQUEST_INFO_SUMMARY_HOUR_";

    /** 年月単位のリクエスト情報ファイルのファイル名のプレフィックス */
    private static final String REQUEST_INFO_SUMMARY_YM_PREFIX = "REQUEST_INFO_SUMMARY_YM_";

    /** リクエストID */
    private static final String REQUEST_ID = "requestId";

    /** 集計単位値 */
    private static final String AGGREGATE_UNIT_VALUE = "aggregateUnitValue";

    /** プロセス名 */
    private static final String PROCESS_NAME = "processName";

    /** 処理リクエスト数 */
    private static final String REQUEST_COUNT = "requestCount";

    /** 処理時間が閾値を超えたリクエスト数 */
    private static final String THRESHOLD_OVER_COUNT = "thresholdOverCount";

    /** 処理時間（平均） */
    private static final String AVERAGE = "average";

    /** 処理時間（中央値） */
    private static final String MEDIAN = "median";

    /** 処理時間（最大値） */
    private static final String MAX = "max";

    /** 日、時間単位の集計結果を保持するMapオブジェクト */
    private final Map<AggregateKey, Aggregator> aggregateResultHolder = new TreeMap<AggregateKey, Aggregator>();

    /** 年月集計結果を保持するMapオブジェクト */
    private final Map<AggregateKey, Aggregator> ymAggregateResultHolder = new TreeMap<AggregateKey, Aggregator>();

    /** プロセス名一覧 */
    private final Set<String> processNames = new HashSet<String>();

    /** オンラインアクセスログ解析処理の設定値 */
    private OnlineStatisticsDefinition logParseDefinition;

    /** システム日付(年月) */
    private String systemDateYm;

    /** 月単位のリクエスト情報ファイルのファイル名 */
    private String requestInfoAggregateYmCsv;

    /** {@inheritDoc} */
    @Override
    protected void initialize(CommandLine command, ExecutionContext context) {
        logParseDefinition = SystemRepository.get(PARSE_DEFINITION_KEY);

        systemDateYm = DateUtil.formatDate(SystemTimeUtil.getDateString(), "yyyyMM");
        requestInfoAggregateYmCsv = REQUEST_INFO_SUMMARY_YM_PREFIX + systemDateYm + '_';
    }

    /**
     * {@inheritDoc}
     * <p/>
     * インプットデータのリクエスト情報を元に、年月、日、時間単位で、処理時間とリクエスト数の算出を行う。
     * ※年月単位の集計は、リクエスト情報の年月が当月(システム日付より取得)の場合のみ集計対象とする。
     */
    @Override
    public Result handle(DataRecord inputData, ExecutionContext ctx) {

        String ymStr = inputData.getString("year") + StringUtil.lpad(inputData.getString("month"), 2, '0');

        if (systemDateYm.equals(ymStr)) {
            // システム日付と同月のデータのみ年月集計を行う。
            int ym = Integer.parseInt(ymStr);
            addRequestInfo(ymAggregateResultHolder, inputData, AggregateUnit.YEAR_MONTH, ym);
        }

        // 日単位の集計処理
        addRequestInfo(aggregateResultHolder, inputData, AggregateUnit.DAY, inputData.getBigDecimal("day").intValue());

        // 時間単位の集計処理
        addRequestInfo(aggregateResultHolder, inputData, AggregateUnit.HOUR, inputData.getBigDecimal("hour")
                .intValue());

        return new Result.Success();
    }

    /**
     * 指定された集計結果ホルダーに今回のリクエスト情報を追加する。
     *
     * @param aggregateHolder 集計結果ホルダー
     * @param inputData リクエスト情報1レコード
     * @param aggregateUnit 集計単位
     * @param unitValue 集計基準値
     */
    private void addRequestInfo(Map<AggregateKey, Aggregator> aggregateHolder,
            DataRecord inputData,
            AggregateUnit aggregateUnit,
            int unitValue) {

        // プロセス名を保持する。
        String processName = inputData.getString(PROCESS_NAME);
        processNames.add(processName);

        // 集計単位のキー
        AggregateKey key = new AggregateKey(
                inputData.getString(REQUEST_ID),
                processName,
                aggregateUnit, unitValue);

        // AggregateKeyは、equals及びhashCodeを実装していないが、使用するMap実装がTreeMapであるため、以下のコードは問題なく動作する。
        // 使用するMap実装が変更になる場合は、AggregateKeyの実装を見直すこと。
        // ※これはカバレッジ対策の実装である。
        if (!aggregateHolder.containsKey(key)) {
            aggregateHolder.put(key, new Aggregator());
        }

        Aggregator aggregator = aggregateHolder.get(key);
        int executeTime = inputData.getBigDecimal("execute_time").intValue();
        aggregator.addRequestData(
                executeTime,
                executeTime > logParseDefinition.getThresholdExecutionTime()       // 閾値を超えているか否か
        );
    }

    /**
     * {@inheritDoc}
     * <p/>
     * 本処理({@link #handle(DataRecord, ExecutionContext)}で集計した結果を、集計結果CSVに出力する。
     * <p/>
     * 集計結果ファイルは、以下の3ファイル出力する。
     * <ul>
     * <li>年月毎集計結果CSV</li>
     * <li>日毎集計結果CSV</li>
     * <li>時間毎集計結果CSV</li>
     * </ul>
     */
    @Override
    protected void terminate(Result result, ExecutionContext context) {

        // プロセス名単位にファイル出力を行う。
        for (String processName : processNames) {
            // 年月単位ファイル
            openOutputCsv(requestInfoAggregateYmCsv + processName + ".csv", "年月");

            // 日単位ファイル
            openOutputCsv(REQUEST_INFO_SUMMARY_DAY_PREFIX + processName + ".csv", "日");

            // 時間単位ファイル
            openOutputCsv(REQUEST_INFO_SUMMARY_HOUR_PREFIX + processName + ".csv", "時間");
        }

        for (Map.Entry<AggregateKey, Aggregator> entry : ymAggregateResultHolder.entrySet()) {
            write(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<AggregateKey, Aggregator> entry : aggregateResultHolder.entrySet()) {
            write(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 出力対象のCSVファイルを開く。
     *
     * @param fileName ファイル名
     * @param aggregateUnitValue 集計単位の値
     */
    private void openOutputCsv(String fileName, String aggregateUnitValue) {
        FileRecordWriterHolder.open(
                logParseDefinition.getRequestInfoSummaryBaseName(),
                fileName,
                logParseDefinition.getRequestInfoSummaryFormatName());

        // CSVタイトルの出力
        Map<String, String> title = new HashMap<String, String>();
        title.put(REQUEST_ID, "リクエストID");
        title.put(AGGREGATE_UNIT_VALUE, aggregateUnitValue);
        title.put(PROCESS_NAME, "プロセス名");
        title.put(REQUEST_COUNT, "処理リクエスト数");
        title.put(THRESHOLD_OVER_COUNT, "処理時間が閾値を超えたリクエスト数");
        title.put(AVERAGE, "処理時間（平均）");
        title.put(MEDIAN, "処理時間（中央値）");
        title.put(MAX, "処理時間（最大値）");
        FileRecordWriterHolder.write(title, logParseDefinition.getRequestInfoSummaryBaseName(), fileName);
    }

    /**
     * 集計結果を集計単位毎のファイルに出力する。
     *
     * @param aggregateKey 集計単位
     * @param aggregator 集計オブジェクト
     */
    private void write(AggregateKey aggregateKey,
            Aggregator aggregator) {

        Map<String, Object> data = new HashMap<String, Object>();
        data.put(REQUEST_ID, aggregateKey.requestId);
        data.put(AGGREGATE_UNIT_VALUE, aggregateKey.aggregateUnitValue);
        data.put(PROCESS_NAME, aggregateKey.processName);
        data.put(REQUEST_COUNT, aggregator.requestCount);
        data.put(THRESHOLD_OVER_COUNT, aggregator.thresholdOverCount);
        data.put(AVERAGE, aggregator.getAverageTime());
        data.put(MAX, aggregator.maxExecutionTime);
        data.put(MEDIAN, aggregator.getMedianTime());

        // 集計単位を元に、出力ファイルを切り替えて集計結果を出力する。
        String fileName = null;
        if (aggregateKey.aggregateUnit == AggregateUnit.YEAR_MONTH) {
            fileName = requestInfoAggregateYmCsv + aggregateKey.processName + ".csv";

        } else if (aggregateKey.aggregateUnit == AggregateUnit.DAY) {
            fileName = REQUEST_INFO_SUMMARY_DAY_PREFIX + aggregateKey.processName + ".csv";

        } else if (aggregateKey.aggregateUnit == AggregateUnit.HOUR) {
            fileName = REQUEST_INFO_SUMMARY_HOUR_PREFIX + aggregateKey.processName + ".csv";
        }
        FileRecordWriterHolder.write(data, logParseDefinition.getRequestInfoSummaryBaseName(), fileName);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * 処理対象のリクエスト情報ファイルをリクエスト情報ファイル格納ディレクトリから抽出し、
     * {@link MultiFileRecordReader}に登録する。
     */
    @Override
    public DataReader<DataRecord> createReader(ExecutionContext ctx) {

        File directory = FilePathSetting.getInstance().getBaseDirectory(logParseDefinition.getRequestInfoBaseName());
        String systemDate = SystemTimeUtil.getDateString();
        String startDate = DateUtil.addMonth(systemDate, logParseDefinition.getAggregatePeriod() * -1);

        List<String> inputFiles = new ArrayList<String>();
        File[] files = FileUtil.listFiles(directory.getAbsolutePath(), "REQUEST_INFO_*");
        for (File file : files) {
            String name = file.getName();
            String fileCreationDate = getFileCreationDate(name);
            if (fileCreationDate != null && fileCreationDate.compareTo(startDate) >= 0) {
                inputFiles.add(file.getName());
            }
        }

        MultiFileRecordReader reader = new MultiFileRecordReader();
        reader.setFileList(logParseDefinition.getRequestInfoBaseName(), inputFiles);
        reader.setLayoutFile(logParseDefinition.getRequestInfoFormatName());
        return reader;
    }

    /**
     * 指定されたファイル名から、ファイルの作成日を抽出する。
     * <p/>
     * ファイル名のルールは、以下の形式となっている必要がある。<br/>
     * 任意の値 + "_" + 作成日 + 拡張子
     *
     * @param fileName ファイル名
     * @return ファイル名から抽出した日付
     */
    private String getFileCreationDate(String fileName) {
        int startPos = fileName.lastIndexOf('_') + 1;
        int lastPos = fileName.lastIndexOf('.');
        if (startPos == -1 || lastPos == -1) {
            return null;
        }
        return fileName.substring(startPos, lastPos);
    }

    /**
     * 集計単位のキー値を保持するクラス。
     * <p/>
     * 本クラスは、Mapのキーとして使用されるクラスであるが、{@link TreeMap}に格納することが前提となっているため、
     * {@link #equals(Object)}及び{@link #hashCode()}の実装は行わない。
     * この前提が変更となった場合は、実装を見直すこと。
     * <p/>
     * キー値は、以下の3種類
     * <ul>
     * <li>リクエストID</li>
     * <li>プロセス名</li>
     * <li>集計単位</li>
     * <li>集計単位値</li>
     * </ul>
     */
    private static final class AggregateKey implements Comparable<AggregateKey> {

        /** リクエストID */
        private final String requestId;

        /** プロセス名 */
        private final String processName;

        /** 集計単位 */
        private final AggregateUnit aggregateUnit;

        /** 集計単位値 */
        private final int aggregateUnitValue;

        /**
         * コンストラクタ。
         *
         * @param requestId リクエストID
         * @param processName プロセス名
         * @param aggregateUnit 集計単位
         * @param aggregateUnitValue 値(集計単位の中で、どの値の集計値か)
         */
        private AggregateKey(String requestId, String processName,
                AggregateUnit aggregateUnit, int aggregateUnitValue) {
            this.requestId = requestId;
            this.processName = processName;
            this.aggregateUnit = aggregateUnit;
            this.aggregateUnitValue = aggregateUnitValue;
        }

        /**
         * {@inheritDoc}
         * <p/>
         * リクエストID->集計値->プロセス名->集計単位の順で比較
         */
        public int compareTo(AggregateKey o) {
            int result = requestId.compareTo(o.requestId);
            if (result == 0) {
                result = aggregateUnitValue - o.aggregateUnitValue;
            }
            if (result == 0) {
                result = processName.compareTo(o.processName);
            }
            if (result == 0) {
                result = aggregateUnit.compareTo(o.aggregateUnit);
            }
            return result;
        }
    }

    /**
     * 集計情報を保持するクラス。
     * <p/>
     * 以下の情報を集計結果として保持する。
     * <ul>
     * <li>リクエスト数</li>
     * <li>閾値({@link OnlineStatisticsDefinition#setThresholdExecutionTime(int)}で設定した閾値)を超えた処理時間のリクエスト数</li>
     * <li>処理時間の平均</li>
     * <li>処理時間の最大値</li>
     * </ul>
     */
    private static final class Aggregator {

        /** 処理時間の合計 */
        private long totalExecutionTime;

        /** 閾値超のリクエスト数 */
        private int thresholdOverCount;

        /** 処理時間のリスト */
        private final List<Integer> executionTimeList = new ArrayList<Integer>();

        /** リクエスト数 */
        private int requestCount;

        /** 最大処理時間 */
        private int maxExecutionTime;

        /**
         * コンストラクタ。
         */
        private Aggregator() {
        }


        /**
         * 集計対象のリクエスト情報を追加する。
         *
         * @param executionTime 処理時間
         * @param thresholdOver 閾値超のリクエストか否か
         */
        private void addRequestData(int executionTime, boolean thresholdOver) {
            requestCount++;
            if (thresholdOver) {
                thresholdOverCount++;
            }
            executionTimeList.add(executionTime);
            maxExecutionTime = Math.max(maxExecutionTime, executionTime);
            totalExecutionTime += executionTime;
        }

        /**
         * 平均処理時間を取得する。
         *
         * @return 平均処理時間
         */
        private int getAverageTime() {
            return (int) (totalExecutionTime / requestCount);
        }

        /**
         * 中央値を取得する。
         *
         * @return 中央値
         */
        private int getMedianTime() {
            Collections.sort(executionTimeList);
            int size = executionTimeList.size();
            int mid = size / 2;
            if (size % 2 == 0) {
                return (executionTimeList.get(mid - 1) + executionTimeList.get(mid)) / 2;
            } else {
                return executionTimeList.get(mid);
            }
        }
    }
}


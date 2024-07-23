package please.change.me.statistics.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nablarch.common.io.FileRecordWriterHolder;
import nablarch.core.date.SystemTimeUtil;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.FileUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Result;
import nablarch.fw.action.NoInputDataBatchAction;
import nablarch.fw.launcher.CommandLine;
import please.change.me.statistics.action.settings.OnlineStatisticsDefinition;
import please.change.me.statistics.exception.LogParseException;

/**
 * 画面オンラインアクセスログファイルの解析バッチアクションクラス。
 * <p/>
 * 本クラスでは、画面オンラインアクセスログを解析し、「リクエスト情報CSV」を出力する。
 * <p/>
 * 本アクションを実行するためには、{@link OnlineStatisticsDefinition}に対して、設定を行うこと。
 * 設定値の詳細は、{@link OnlineStatisticsDefinition}を参照。
 * {@link OnlineStatisticsDefinition}は、リポジトリにキー値「onlineAccessLogParseDefinition」で登録しておくこと。
 * 以下に設定ファイルの例を示す。
 * <pre>
 * {@code
 * <component name="onlineAccessLogParseDefinition"
 *     class="please.change.me.statistics.action.settings.OnlineStatisticsDefinition">
 *   <property name="accessLogDir" value="${access-log.dir}" />
 *   <property name="accessLogFileNamePattern" value="${access-log.name.pattern}" />
 *   <property name="accessLogParseDir" value="${access-log.parse.temp.dir}" />
 *   <property name="endLogPattern" value="@@@@ END @@@@" />
 *   <property name="includeRequestIdList">
 *     <list>
 *       <value>RGH6AAM105</value>
 *       <value>RGH6AAR302</value>
 *       <value>RGH6AAR402</value>
 *     </list>
 *   </property>
 *   <property name="findRequestIdPattern" value="rid = \[([a-zA-Z0-9_]+)\]" />
 *   <property name="findProcessNamePattern" value="process_name = \[([A-Z0-9]+)\]" />
 *   <property name="findStatusCodePattern" value="status_code = \[([0-9]+)\]" />
 *   <property name="logOutputDateTimeStartPosition" value="0" />
 *   <property name="logOutputDateTimeEndPosition" value="23" />
 *   <property name="findExecutionTimePattern" value="execution_time = \[([0-9]+)\]" />
 *   <property name="requestInfoFormatName" value="requestInfo" />
 *   <property name="requestInfoBaseName" value="requestInfo.dir" />
 * </component>
 * }
 * </pre>
 *
 * @author hisaaki sioiri
 */
public class OnlineAccessLogParseAction extends NoInputDataBatchAction {

    /** オンラインアクセスログ解析処理の設定値を{@link SystemRepository}から取得するためのキー値 */
    private static final String PARSE_DEFINITION_KEY = "onlineAccessLogParseDefinition";

    /** オンラインアクセスログ解析処理の設定値 */
    private OnlineStatisticsDefinition logParseDefinition;

    /** リクエスト情報ファイルのファイル名 */
    private String requestInfoCsv;

    /**
     * {@inheritDoc}
     * <p/>
     * 以下の処理を行う。
     * <ul>
     * <li>アクセスログを解析用の一時ディレクトリにコピーする</li>
     * <li>終了ログ(ENDログ)を特定するための正規表現のコンパイル</li>
     * <li>リクエスト情報CSVを開く</li>
     * </ul>
     */
    @Override
    protected void initialize(CommandLine command, ExecutionContext context) {

        logParseDefinition = SystemRepository.get(PARSE_DEFINITION_KEY);

        // 解析用に一時ディレクトリにアクセスログをコピーする。
        copyAccessLog();

        // リクエスト情報を出力用に開く
        requestInfoCsv = "REQUEST_INFO_" + SystemTimeUtil.getDateString() + ".csv";
        FileRecordWriterHolder.open(logParseDefinition.getRequestInfoBaseName(), requestInfoCsv,
                logParseDefinition.getRequestInfoFormatName());
    }

    /**
     * {@inheritDoc}
     * <p/>
     * 作業ディレクトリにコピーしたアクセスログファイルを削除する。
     */
    @Override
    protected void terminate(Result result, ExecutionContext context) {
        String tempDir = logParseDefinition.getAccessLogParseDir();
        for (File file : listFiles(tempDir)) {
            FileUtil.deleteFile(file);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * アクセスログを解析し、リクエスト情報中間ファイルを出力する。
     */
    @Override
    public Result handle(ExecutionContext ctx) {
        String tempDir = logParseDefinition.getAccessLogParseDir();
        for (File file : listFiles(tempDir)) {
            parseAccessLog(file);
        }
        return new Result.Success();
    }

    /**
     * アクセスログの解析処理を行う。
     *
     * @param file 解析対象のアクセスログ
     */
    private void parseAccessLog(File file) {
        InputStream resource = FileUtil.getResource(file.toURI().toString());
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!isEndLog(line)) {
                    // 終了ログ以外は処理しない
                    continue;
                }

                String requestId = findRequestId(line);
                if (!isTargetRequestId(requestId)) {
                    // 処理対象外のリクエストIDの場合はスキップ
                    continue;
                }

                Map<String, Object> outputData = new HashMap<String, Object>();
                outputData.put("requestId", requestId);
                Calendar dateTime = findLogOutputDateTime(line);
                outputData.put("year", dateTime.get(Calendar.YEAR));
                outputData.put("month", dateTime.get(Calendar.MONTH) + 1);
                outputData.put("day", dateTime.get(Calendar.DATE));
                outputData.put("hour", dateTime.get(Calendar.HOUR_OF_DAY));
                outputData.put("processName", findProcessName(line));
                outputData.put("statusCode", findStatusCode(line));
                outputData.put("executeTime", findExecutionTime(line, reader));

                FileRecordWriterHolder.write(outputData, logParseDefinition.getRequestInfoBaseName(),
                        requestInfoCsv);
            }
        } catch (Exception e) {
            throw new LogParseException("failed to read access log file. file = [" + file.getAbsolutePath() + ']', e);
        } finally {
            FileUtil.closeQuietly(resource);
        }
    }

    /**
     * ステータスコードを抽出する。
     *
     * @param line 抽出対象の行
     * @return 抽出したステータスコード
     */
    private String findStatusCode(String line) {
        Matcher matcher = logParseDefinition.getFindStatusCodePattern().matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new LogParseException("online access log parse error. STATUS_CODE was not found in end log.");
    }

    /**
     * ログ出力日時を抽出する。
     *
     * @param line 抽出対象の行
     * @return ログ出力日時
     * @throws ParseException 日付解析処理で例外が発生した場合
     */
    private Calendar findLogOutputDateTime(String line) throws ParseException {

        String dateTime = line.substring(
                logParseDefinition.getLogOutputDateTimeStartPosition(),
                logParseDefinition.getLogOutputDateTimeEndPosition());
        DateFormat format = new SimpleDateFormat(logParseDefinition.getLogOutputDateTimeFormat());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(format.parse(dateTime));
        return calendar;
    }

    /**
     * 起動プロセス名を抽出する。
     *
     * プロセス名がログから取得できない場合には、空文字列を返却する。
     *
     * @param line 抽出対象の行
     * @return 起動プロセス名
     */
    private String findProcessName(String line) {
        Matcher matcher = logParseDefinition.getFindProcessNamePattern().matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * リクエストIDを抽出する。
     * <p/>
     * リクエストIDが見つからない場合は、nullを返却する。
     *
     * @param line 抽出対象の行
     * @return リクエストID
     */
    private String findRequestId(String line) {
        Matcher matcher = logParseDefinition.getFindRequestIdPattern().matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new LogParseException("online access log parse error. REQUEST_ID was not found in end log.");
    }

    /**
     * 処理時間を取得する。
     *
     * @param line 抽出対象の行
     * @param reader リーダ
     * @return 処理時間
     * @throws IOException ファイル読み込みに失敗した場合
     */
    private String findExecutionTime(String line, BufferedReader reader) throws IOException {
        Pattern pattern = logParseDefinition.getFindExecutionTimePattern();
        Matcher matcher = pattern.matcher(line);
        while (!matcher.find()) {
            line = reader.readLine();
            if (line == null) {
                throw new LogParseException("online access log parse error. EXECUTION_TIME was not found in end log.");
            }
            matcher = pattern.matcher(line);
        }
        return matcher.group(1);
    }

    /**
     * 抽出対象のリクエストIDか判定する。
     *
     * @param requestId リクエストID
     * @return 抽出対象のリクエストIDの場合はtrue
     */
    private boolean isTargetRequestId(String requestId) {
        return logParseDefinition.getIncludeRequestIdList().contains(requestId);
    }

    /**
     * 終了ログか否かを判定する。
     *
     * @param line 判定対象の行
     * @return 終了ログの場合は、true
     */
    private boolean isEndLog(String line) {
        return logParseDefinition.getEndLogPattern().matcher(line).find();
    }

    /**
     * 指定されたディレクトリ配下から、アクセスログ名に一致するファイルを抽出する。
     * <p/>
     * 抽出されたファイルは、最終更新日時の昇順でソートし返却する。
     *
     * @param dir ディレクトリ
     * @return ファイル一覧
     */
    private File[] listFiles(String dir) {
        File[] files = FileUtil.listFiles(dir, logParseDefinition.getAccessLogFileNamePattern());
        List<File> fileList = new ArrayList<File>();
        for (File file : files) {
            if (file.isFile()) {
                fileList.add(file);
            }
        }

        // 最終更新日時でソートする。
        Collections.sort(fileList, new Comparator<File>() {
            public int compare(File o1, File o2) {
                String name1 = o1.getName();
                String name2 = o2.getName();
                if (name1.length() == name2.length()) {
                    return name1.compareTo(name2);
                } else {
                    // 短いファイル名は最新ファイルとして、判断する。
                    return name2.length() - name1.length();
                }
            }
        });
        return fileList.toArray(new File[fileList.size()]);
    }

    /** アクセスログを解析用の一時ディレクトリにコピーする。 */
    private void copyAccessLog() {
        String accessLogDir = logParseDefinition.getAccessLogDir();
        String tempDir = logParseDefinition.getAccessLogParseDir();

        for (File file : listFiles(accessLogDir)) {
            FileUtil.copy(file, new File(tempDir + '/' + file.getName()));
        }
    }
}


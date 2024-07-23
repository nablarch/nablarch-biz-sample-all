package please.change.me.statistics.action.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * オンラインアクセスログを解析するための設定を保持するクラス。
 * <p/>
 * 設定値の詳細は、本クラスの各setterメソッドを参照。
 *
 * @author hisaaki sioiri
 */
public class OnlineStatisticsDefinition {

    /** アクセスログディレクトリ */
    private String accessLogDir;

    /** アクセスログファイル名パターン */
    private String accessLogFileNamePattern;

    /** アクセスログ解析用の一時ディレクトリ */
    private String accessLogParseDir;

    /** アクセスログの終了ログ(ENDログ)を特定するための正規表現 */
    private Pattern endLogPattern;

    /** 抽出対象のリクエストID一覧 */
    private List<String> includeRequestIdList = new ArrayList<String>();

    /** 終了ログからリクエストIDを抽出するための正規表現 */
    private Pattern findRequestIdPattern;

    /** 終了ログからプロセス名を抽出するための正規表現 */
    private Pattern findProcessNamePattern;

    /** ステータスコードを抽出するための正規表現 */
    private Pattern findStatusCodePattern;

    /** リクエスト処理時間を抽出するための正規表現 */
    private Pattern findExecutionTimePattern;

    /** 終了ログ出力日時の開始位置 */
    private int logOutputDateTimeStartPosition;

    /** 終了ログ出力日時の終了位置 */
    private int logOutputDateTimeEndPosition;

    /** 終了ログのフォーマット */
    private String logOutputDateTimeFormat;

    /** 出力するリクエスト情報CSVのフォーマット定義ファイル名 */
    private String requestInfoFormatName;

    /** リクエスト情報CSVの格納先ディレクトリの論理名 */
    private String requestInfoBaseName;

    /** リクエスト情報サマリー結果CSVの格納先ディレクトリの論理名 */
    private String requestInfoSummaryBaseName;

    /** リクエスト情報サマリー結果のフォーマット定義ファイル名 */
    private String requestInfoSummaryFormatName;

    /** リクエスト処理時間の閾値(この時間を超えたリクエスト数をカウントする) */
    private int thresholdExecutionTime;

    /** 集計対象期間 */
    private int aggregatePeriod = 1;

    /**
     * アクセスログが出力されるディレクトリを設定する。
     * <p/>
     * アクセスログ出力ディレクトリは、アプリケーションがアクセスログを出力するディレクトリ名を、
     * 絶対または相対パスで設定する。
     *
     * @param accessLogDir アクセスログ出力ディレクトリ
     */
    public void setAccessLogDir(String accessLogDir) {
        this.accessLogDir = accessLogDir;
    }

    /**
     * アクセスログ格納ディレクトリを取得する。
     *
     * @return アクセスログ格納ディレクトリ
     */
    public String getAccessLogDir() {
        return accessLogDir;
    }

    /**
     * アクセスログのファイル名パターンを取得する。
     *
     * @return アクセスログのファイル名パターン
     */
    public String getAccessLogFileNamePattern() {
        return accessLogFileNamePattern;
    }

    /**
     * アクセスログのファイル名パターンを設定する。
     * <p/>
     * 任意の値を指定したい場合には、「*」を使用する。（正規表現とは指定方法が異なるため注意すること。）
     * 例えば、アクセスログファイル名が「access」から始まっているのであれば、
     * 「access*」と設定すれば良い。
     *
     * @param accessLogFileNamePattern アクセスログのファイル名パターン
     */
    public void setAccessLogFileNamePattern(String accessLogFileNamePattern) {
        this.accessLogFileNamePattern = accessLogFileNamePattern;
    }

    /**
     * アクセスログ解析用ディレクトリ名を取得する。
     *
     * @return アクセスログ解析用ディレクトリ名
     */
    public String getAccessLogParseDir() {
        return accessLogParseDir;
    }

    /**
     * アクセスログ解析用ディレクトリ名を設定する。
     *
     * @param accessLogParseDir アクセスログ解析用ディレクトリ
     */
    public void setAccessLogParseDir(String accessLogParseDir) {
        this.accessLogParseDir = accessLogParseDir;
    }

    /**
     * 終了ログを特定するための正規表現パターン
     *
     * @return 終了ログを特定するための正規表現パターン
     */
    public Pattern getEndLogPattern() {
        return endLogPattern;
    }

    /**
     * 終了ログを特定するための正規表現パターンを設定する。
     *
     * @param endLogPattern 終了ログを特定するための正規表現パターン
     */
    public void setEndLogPattern(String endLogPattern) {
        this.endLogPattern = Pattern.compile(endLogPattern);
    }

    /**
     * 抽出対象のリクエストIDのリストを取得する。
     *
     * @return 抽出対象のリクエストIDのリスト
     */
    public List<String> getIncludeRequestIdList() {
        return includeRequestIdList;
    }

    /**
     * リクエスト情報として抽出する対象のリクエストIDのリストを設定する。
     * <p/>
     * ここで設定されたリクエストIDが、アクセスログから抽出される対象となる。
     *
     * @param includeRequestIdList 抽出対象のリクエストIDのリスト
     */
    public void setIncludeRequestIdList(List<String> includeRequestIdList) {
        this.includeRequestIdList = Collections.unmodifiableList(includeRequestIdList);
    }

    /**
     * リクエストIDを抽出するための正規表現を取得する。
     *
     * @return リクエストIDを抽出するための正規表現
     */
    public Pattern getFindRequestIdPattern() {
        return findRequestIdPattern;
    }

    /**
     * リクエストIDを抽出するための正規表現を設定する。
     * <p/>
     * 設定する正規表現には、リクエストIDを示す部分をグループ化すること。
     * 例えば、リクエストIDが「request_id = [RG123456]」のように出力される場合には、
     * 設定する正規表現は「request_id = \[([A-Z0-9]+)\]」とすれば良い。
     * これにより、グループ化された角括弧内のリクエストIDが抽出できる。
     *
     * @param findRequestIdPattern リクエストIDを抽出するための正規表現
     */
    public void setFindRequestIdPattern(String findRequestIdPattern) {
        this.findRequestIdPattern = Pattern.compile(findRequestIdPattern);
    }

    /**
     * 終了ログからプロセス名を抽出するための正規表現を取得する。
     *
     * @return プロセス名を抽出するための正規表現
     */
    public Pattern getFindProcessNamePattern() {
        return findProcessNamePattern;
    }

    /**
     * 終了ログからプロセス名を抽出するための正規表現を設定する。
     * <p/>
     * 設定する正規表現には、プロセス名を示す部分をグループ化すること。
     * 例えば、プロセス名が「process_name = [APP_1]」のように出力される場合には、
     * 設定する正規表現は「process_name = \[([A-Z0-9_]+)\]」とすれば良い。
     * これにより、グループ化された角括弧内のプロセス名が抽出できる。
     *
     * @param findProcessNamePattern プロセス名を抽出するための正規表現
     */
    public void setFindProcessNamePattern(String findProcessNamePattern) {
        this.findProcessNamePattern = Pattern.compile(findProcessNamePattern);
    }

    /**
     * 終了ログからステータスコードを抽出するための正規表現を取得する。
     *
     * @return スータスコードを抽出するための正規表現
     */
    public Pattern getFindStatusCodePattern() {
        return findStatusCodePattern;
    }

    /**
     * 終了ログからステータスコードを取得するための正規表現
     * <p/>
     * 設定する正規表現には、ステータスコードを示す部分をグループ化すること。
     * 例えば、ステータスコードが「status_code = [200]」のように出力される場合には、
     * 設定する正規表現は「status_code = \[([0-9]+)\]」とすれば良い。
     * これにより、グループ化された角括弧内のステータスコードが抽出できる。
     *
     * @param findStatusCodePattern ステータスコードを抽出するための正規表現
     */
    public void setFindStatusCodePattern(String findStatusCodePattern) {
        this.findStatusCodePattern = Pattern.compile(findStatusCodePattern);
    }

    /**
     * 終了ログからリクエスト処理時間を抽出するための正規表現を設定する。
     *
     * @return 終了ログからリクエスト処理時間を抽出するための正規表現
     */
    public Pattern getFindExecutionTimePattern() {
        return findExecutionTimePattern;
    }

    /**
     * 終了ログからリクエスト処理時間を抽出するための正規表現を設定する。
     * <p/>
     * 設定する正規表現には、処理時間を示す部分をグループ化すること。
     * 例えば、処理時間が「execution_time = [50]」のように出力される場合には、
     * 設定する正規表現は「execution_time = \[([0-9]+)\]」とすれば良い。
     * これにより、グループ化された角括弧内の処理時間が抽出できる。
     *
     * @param findExecutionTimePattern リクエスト処理時間を抽出するための正規表現
     */
    public void setFindExecutionTimePattern(String findExecutionTimePattern) {
        this.findExecutionTimePattern = Pattern.compile(findExecutionTimePattern);
    }

    /**
     * ログ出力日時にログ出力ポジション（開始位置）を取得する。
     *
     * @return ログ出力日時のログ出力ポジション（開始位置）
     */
    public int getLogOutputDateTimeStartPosition() {
        return logOutputDateTimeStartPosition;
    }

    /**
     * ログ出力日時のログ出力ポジション（開始位置）を設定する。
     *
     * @param logOutputDateTimeStartPosition ログ出力日時のログ出力ポジション（開始位置）
     */
    public void setLogOutputDateTimeStartPosition(int logOutputDateTimeStartPosition) {
        this.logOutputDateTimeStartPosition = logOutputDateTimeStartPosition;
    }

    /**
     * ログ出力日時にログ出力ポジション（終了位置）を取得する。
     *
     * @return ログ出力日時のログ出力ポジション（終了位置）
     */
    public int getLogOutputDateTimeEndPosition() {
        return logOutputDateTimeEndPosition;
    }

    /**
     * ログ出力日時のログ出力ポジション（終了位置）を設定する。
     *
     * @param logOutputDateTimeEndPosition ログ出力日時のログ出力ポジション（終了位置）
     */
    public void setLogOutputDateTimeEndPosition(int logOutputDateTimeEndPosition) {
        this.logOutputDateTimeEndPosition = logOutputDateTimeEndPosition;
    }

    /**
     * 終了ログのフォーマットを取得する。
     *
     * @return 終了ログのフォーマット
     */
    public String getLogOutputDateTimeFormat() {
        return logOutputDateTimeFormat;
    }

    /**
     * 終了ログのフォーマットを設定する。
     * <p/>
     * {@link java.text.SimpleDateFormat}で日時を解析できるフォイ－マットを設定すること。
     * 設定されたフォーマットを使用してログ出力日時を解析し、各要素(年、月、日、時)に分割を行う。
     *
     * @param logOutputDateTimeFormat 終了ログのフォーマット
     */
    public void setLogOutputDateTimeFormat(String logOutputDateTimeFormat) {
        this.logOutputDateTimeFormat = logOutputDateTimeFormat;
    }

    /**
     * リクエスト情報CSVのフォーマット定義ファイル名を取得する。
     *
     * @return リクエスト情報CSVのフォーマット定義
     */
    public String getRequestInfoFormatName() {
        return requestInfoFormatName;
    }

    /**
     * リクエスト情報CSVのフォーマット定義ファイル名を設定する。
     *
     * @param requestInfoFormatName リクエスト情報CSVのフォーマット定義
     */
    public void setRequestInfoFormatName(String requestInfoFormatName) {
        this.requestInfoFormatName = requestInfoFormatName;
    }

    /**
     * リクエスト情報CSVの格納先ディレクトリの論理名を取得する。
     *
     * @return リクエスト情報CSVの格納先ディレクトリの論理名
     */
    public String getRequestInfoBaseName() {
        return requestInfoBaseName;
    }

    /**
     * リクエスト情報CSVの格納先ディレクトリの論理名を設定する。
     *
     * @param requestInfoBaseName リクエスト情報CSVの格納先ディレクトリの論理名
     */
    public void setRequestInfoBaseName(String requestInfoBaseName) {
        this.requestInfoBaseName = requestInfoBaseName;
    }

    /**
     * リクエスト情報サマリー結果CSVの格納先ディレクトリの論理名を取得する。
     *
     * @return リクエスト情報サマリー結果CSVの格納先ディレクトリの論理名
     */
    public String getRequestInfoSummaryBaseName() {
        return requestInfoSummaryBaseName;
    }

    /**
     * リクエスト情報サマリー結果CSVの格納先ディレクトリの論理名を設定する。
     *
     * @param requestInfoSummaryBaseName リクエスト情報サマリー結果CSVの格納先ディレクトリの論理名
     */
    public void setRequestInfoSummaryBaseName(String requestInfoSummaryBaseName) {
        this.requestInfoSummaryBaseName = requestInfoSummaryBaseName;
    }

    /**
     * リクエスト情報サマリー結果のフォーマット定義ファイル名を取得する。
     *
     * @return リクエスト情報サマリー結果のフォーマット定義ファイル名
     */
    public String getRequestInfoSummaryFormatName() {
        return requestInfoSummaryFormatName;
    }

    /**
     * リクエスト情報サマリー結果のフォーマット定義ファイル名を設定する。
     *
     * @param requestInfoSummaryFormatName リクエスト情報サマリー結果のフォーマット定義ファイル名
     */
    public void setRequestInfoSummaryFormatName(String requestInfoSummaryFormatName) {
        this.requestInfoSummaryFormatName = requestInfoSummaryFormatName;
    }

    /**
     * リクエスト処理時間の閾値を取得する。
     *
     * @return リクエスト処理時間の閾値
     */
    public int getThresholdExecutionTime() {
        return thresholdExecutionTime;
    }

    /**
     * リクエスト処理時間の閾値を設定する。
     * <p/>
     * ここで設定した閾値を超えたリクエスト数をカウントするために使用する。
     *
     * @param thresholdExecutionTime リクエスト処理時間の閾値
     */
    public void setThresholdExecutionTime(int thresholdExecutionTime) {
        this.thresholdExecutionTime = thresholdExecutionTime;
    }

    /**
     * 集計期間を取得する。
     *
     * @return 集計期間
     */
    public int getAggregatePeriod() {
        return aggregatePeriod;
    }

    /**
     * 集計期間を設定する。
     * <p/>
     * 各何ヶ月分のリクエスト情報CSVを集計対象に含めるかを設定する。
     * 例えば、2と設定した場合システム日付を基準として2ヶ月前の日付以降に生成されたリクエスト情報CSVが集計対象となる。
     * <p/>
     * なお、本設定値を省略した場合の集計期間は1ヶ月となる。
     *
     * @param aggregatePeriod 集計期間
     */
    public void setAggregatePeriod(int aggregatePeriod) {
        this.aggregatePeriod = aggregatePeriod;
    }
}


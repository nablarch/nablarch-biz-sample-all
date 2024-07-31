package please.change.me.test;

import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.test.core.http.AbstractHttpRequestTestTemplate;
import nablarch.test.core.http.Advice;
import nablarch.test.core.http.HttpRequestTestSupport;
import nablarch.test.core.http.TestCaseInfo;

/**
 * 本プロジェクト専用の{@link HttpRequestTestSupport}継承クラス。<br/>
 * プロジェクト固有の共通処理を提供する。
 * 画面オンライン処理方式のリクエスト単体テストを作成する場合、
 * {@link HttpRequestTestSupport}を直接使用するのではなく、
 * 本クラスを使用すること。
 */
// TODO XxxxをPJ名に変更してください(例:MyProjectHttpRequestTestSupport)。
public class XxxxHttpRequestTestSupport
        extends AbstractHttpRequestTestTemplate<TestCaseInfo> {

    private final String baseUri;

    /**
     * コンストラクタ。
     * @param testClass テストクラス
     * @param baseUri ベースURI
     */
    public XxxxHttpRequestTestSupport(Class<?> testClass, String baseUri) {
        super(testClass);
        this.baseUri = baseUri;
    }

    @Override
    protected String getBaseUri() {
        return baseUri;
    }

    /**
     * リクエスト単体時には、リクエストパラメータに排他制御情報を記載しなくてすむようにするため、
     * 一律、リクエストパラメータにダミーのバージョン情報を格納する。
     *
     * @param testCaseInfo テストケース情報
     * @param context      ExecutionContextインスタンス
     * @param advice       実行前後の処理を実装した{@link Advice}
     */
    @Override
    protected void beforeExecuteRequest(TestCaseInfo testCaseInfo, ExecutionContext context, Advice<TestCaseInfo> advice) {
        super.beforeExecuteRequest(testCaseInfo, context, advice);
        HttpRequest request = testCaseInfo.getHttpRequest();
        request.setParam("nablarch_version", "version=1|versionColumnName=VERSION");
    }
}

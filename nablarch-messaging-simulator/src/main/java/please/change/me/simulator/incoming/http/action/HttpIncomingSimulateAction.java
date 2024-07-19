package please.change.me.simulator.incoming.http.action;

import nablarch.core.repository.SystemRepository;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import please.change.me.simulator.common.MessageReadSupport;

/**
 * HTTPメッセージ受信シミュレートアクション。
 *
 * @author Ryo TANAKA
 * @since 1.4.2
 */
public class HttpIncomingSimulateAction implements Handler<HttpRequest, HttpResponse> {

    /** サポートクラス */
    private final MessageReadSupport messageReadSupport = new MessageReadSupport();

    /** {@inheritDoc} */
    @Override
    public HttpResponse handle(HttpRequest request, ExecutionContext context) {
        String id = getRequestId(request);

        HttpResponse response = messageReadSupport.getResponseForHttp(id);
        return response;
    }

    /**
     * リクエストID(読み込むExcelファイル)を返す。<br>
     * @param request リクエストオブジェクト
     * @return リクエストID
     */
    protected String getRequestId(HttpRequest request) {
        return SystemRepository.getString("request-id");
    }

}

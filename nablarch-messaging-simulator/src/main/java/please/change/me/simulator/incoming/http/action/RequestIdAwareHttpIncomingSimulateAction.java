package please.change.me.simulator.incoming.http.action;

import nablarch.fw.results.BadRequest;
import nablarch.fw.web.HttpRequest;

/**
 * HTTPメッセージ受信シミュレートアクション。
 * リクエストパラメータに{@literal "requestId"}が含まれている場合、
 * そのリクエストIDに対応するレスポンスを返却する。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class RequestIdAwareHttpIncomingSimulateAction extends HttpIncomingSimulateAction {

    /** {@inheritDoc} */
    @Override
    protected String getRequestId(HttpRequest request) {
        String[] requestIds = request.getParam("requestId");
        if (requestIds == null || requestIds.length < 1) {
            throw new BadRequest("parameter 'requestId' must be set.");
        }
        return requestIds[0];
    }

}

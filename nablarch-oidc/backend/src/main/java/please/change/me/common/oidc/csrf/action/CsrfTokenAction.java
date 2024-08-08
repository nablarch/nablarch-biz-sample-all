package please.change.me.common.oidc.csrf.action;

import nablarch.common.web.csrf.CsrfTokenUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import please.change.me.common.oidc.csrf.form.CsrfTokenResponseForm;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/csrf_token")
public class CsrfTokenAction {


    /**
     * CSRFトークンの設定情報を取得する。
     *
     * @param context 実行コンテキスト
     * @return CSRFトークン設定情報
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CsrfTokenResponseForm get(HttpRequest request, ExecutionContext context) {
        String csrfToken = CsrfTokenUtil.getCsrfToken(context);
        String headerName = CsrfTokenUtil.getHeaderName();

        return new CsrfTokenResponseForm(csrfToken, headerName);
    }
}

package please.change.me.common.oidc.verification.adb2c.action;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import nablarch.common.web.csrf.CsrfTokenUtil;
import nablarch.common.web.session.SessionUtil;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.repository.SystemRepository;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpResponse;
import please.change.me.common.oidc.verification.adb2c.form.LoginRequestForm;
import please.change.me.common.oidc.verification.adb2c.jwt.IdTokenVerifier;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/adb2c/login")
public class LoginAction {

    private static final Logger LOGGER = LoggerManager.get("DEV");

    /**
     * IDトークンで認証を行い、成功すればログインセッションを確立する。
     *
     * @param context 実行コンテキスト
     * @param form リクエストボディ
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Valid
    public void login(ExecutionContext context, LoginRequestForm form) {
        // IDトークンが有効であるか検証する
        DecodedJWT decodedJWT = verifyIdToken(form.getIdToken());

        // 安全性向上のため、認証成功後にセッションIDおよびCSRFトークンを変更する
        SessionUtil.changeId(context);
        CsrfTokenUtil.regenerateCsrfToken(context);

        // IDトークンで連携された情報からユーザー情報を特定して、認証状態をセッションに保持する
        String userId = decodedJWT.getSubject();
        SessionUtil.put(context, "user.id", userId);
    }

    /**
     * IDトークンが有効であるか検証する。
     *
     * @param idToken IDトークン
     * @return デコード済みのIDトークン
     * @throws HttpErrorResponse 無効なIDトークンである場合（HTTPステータスコードは401）
     */
    private DecodedJWT verifyIdToken(String idToken) {
        // プロパティを使用した検証用コンポーネントを定義しているため、システムリポジトリから取得する
        IdTokenVerifier idTokenVerifier = SystemRepository.get("adb2cIdTokenVerifier");
        try {
            // IDトークンを検証する
            return idTokenVerifier.verify(idToken);
        } catch (JWTVerificationException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.logDebug("ID token verification failed...", e);
            }
            // 検証で異常を検知した場合は、ステータスコードが401(Unauthorized)のエラーレスポンスを返却する
            throw new HttpErrorResponse(HttpResponse.Status.UNAUTHORIZED.getStatusCode());
        }
    }
}

package please.change.me.common.oidc.verification.adb2c.form;

import nablarch.core.validation.ee.Required;

import java.io.Serializable;

/**
 * ログイン入力フォーム。
 */
public class LoginRequestForm implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * IDトークン
     */
    @Required
    private String idToken;

    /**
     * IDトークンを取得する。
     *
     * @return IDトークン
     */
    public String getIdToken() {
        return idToken;
    }

    /**
     * IDトークンを設定する。
     *
     * @param idToken IDトークン
     */
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}

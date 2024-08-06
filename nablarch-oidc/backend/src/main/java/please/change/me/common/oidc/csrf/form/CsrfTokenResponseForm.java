package please.change.me.common.oidc.csrf.form;

public class CsrfTokenResponseForm {

    /**
     * CSRFトークン
     */
    private final String csrfTokenValue;

    /**
     * CSRFトークンを設定する際のHTTPヘッダー名
     */
    private final String csrfTokenHeaderName;

    /**
     * コンストラクタ。
     *
     * @param csrfTokenValue CSRFトークン
     * @param csrfTokenHeaderName CSRFトークンを設定する際のHTTPヘッダー名
     */
    public CsrfTokenResponseForm(String csrfTokenValue, String csrfTokenHeaderName) {
        this.csrfTokenValue = csrfTokenValue;
        this.csrfTokenHeaderName = csrfTokenHeaderName;
    }

    /**
     * CSRFトークンを取得する。
     *
     * @return CSRFトークン
     */
    public String getCsrfTokenValue() {
        return csrfTokenValue;
    }

    /**
     * CSRFトークンを設定する際のHTTPヘッダー名を取得する。
     *
     * @return CSRFトークンを設定する際のHTTPヘッダー名
     */
    public String getCsrfTokenHeaderName() {
        return csrfTokenHeaderName;
    }

}

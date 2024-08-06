package please.change.me.common.oidc.verification.adb2c.jwt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenIDメタデータ。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenIdMetaData {

    /**
     * JWKSのURL。
     */
    @JsonProperty("jwks_uri")
    private String jwksUri;

    /**
     * JWKSのURLを取得する。
     *
     * @return JWKSのURL
     */
    public String getJwksUri() {
        return jwksUri;
    }

    /**
     * JWKSのURLを設定する。
     *
     * @param jwksUri JWKSのURL
     */
    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }
}

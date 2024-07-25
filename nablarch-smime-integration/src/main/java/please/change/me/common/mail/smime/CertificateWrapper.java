package please.change.me.common.mail.smime;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Enumeration;

import nablarch.core.repository.initialization.Initializable;
import nablarch.core.util.FileUtil;
import nablarch.core.util.StringUtil;

/**
 * 電子署名付きメール用の証明書をロードするクラス。
 * <p/>
 * 本クラスでは、証明書ファイルをロードし証明書及び公開鍵、秘密鍵の取得を簡易的に行える機能を提供する。
 *
 * @author hisaaki sioiri
 * @see KeyStore
 */
public class CertificateWrapper implements Initializable {

    /** キーストアタイプ */
    private String keyStoreType;

    /** 証明書にアクセスするためのパスワード */
    private String password;

    /** キーストア */
    private KeyStore keyStore;

    /** 証明書 */
    private Certificate[] certificates;

    /** 証明書を取得するためのalias名 */
    private String alias;

    /** 証明書ファイル名 */
    private String certificateFileName;

    /** 秘密鍵 */
    private PrivateKey privateKey;

    /** 秘密鍵にアクセスする際に使用するパスワード */
    private String keyPassword;

    /**
     * KeyStoreタイプを設定する。
     *
     * @param keyStoreType KeyStoreタイプ
     */
    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    /**
     * 証明書を取得するためのalias名を設定する。
     * <p/>
     * alias名が設定されていない場合は、証明書内で最初に見つかった鍵情報のalias名を使用する。
     *
     * @param alias alias名
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * 証明書にアクセスするためのパスワードを設定する。
     *
     * @param password 証明書にアクセスするためのパスワード
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 秘密鍵にアクセスするためのパスワードを設定する。
     *
     * @param keyPassword 秘密鍵にアクセスするためのパスワード
     */
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    /**
     * 証明書ファイル名を設定する。
     *
     * @param certificateFileName 証明書ファイル名
     */
    public void setCertificateFileName(String certificateFileName) {
        this.certificateFileName = certificateFileName;
    }

    /**
     * 証明書チェーンを取得する。
     *
     * @return 証明書チェーン
     */
    public Certificate[] getCertificates() {
        return certificates;
    }

    /**
     * ルート証明書を取得する。
     *
     * @return ルート証明書
     */
    public Certificate getRootCertificate() {
        return certificates[0];
    }

    /**
     * 秘密鍵を取得する。
     *
     * @return 秘密鍵
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * 初期化処理を行う。
     * <p/>
     * 初期化処理では、本クラスに設定された情報を元に電子署名ファイル(PKCS12やJKS形式のファイル)から、証明書情報及び秘密鍵を取得する。
     */
    public void initialize() {

        InputStream stream = null;
        try {
            stream = FileUtil.getResource(certificateFileName);
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(stream, password.toCharArray());

            if (StringUtil.isNullOrEmpty(alias)) {
                // aliasが設定されていない場合は、最初のkeyのaliasを使用する。
                alias = getDefaultAliasName();
            } else {
                validateAlias();
            }
            certificates = keyStore.getCertificateChain(alias);
            privateKey = (PrivateKey) keyStore.getKey(alias, keyPassword.toCharArray());
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(
                    "Certificate file access error. fileName = [" + certificateFileName + ']', e);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Certificate file access error. fileName = [" + certificateFileName + ']', e);
        } finally {
            FileUtil.closeQuietly(stream);
        }
    }

    /**
     * デフォルトのエイリアス名を取得する。
     * <p/>
     * キーストア内の各エントリの中で、最初のキーエントリのエイリアス名を
     * デフォルトのエイリアス名として返却する。
     *
     * @return デフォルトのエイリアス名
     * @throws KeyStoreException キーストアへのアクセスに失敗した場合
     */
    private String getDefaultAliasName() throws KeyStoreException {
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String keyStoreAlias = aliases.nextElement();
            if (keyStore.isKeyEntry(keyStoreAlias)) {
                return keyStoreAlias;
            }
        }
        // デフォルトのaliasが見つからない場合はエラー
        throw new IllegalArgumentException(
                "key entry was not found in certificate file. fileName = [" + certificateFileName + ']');
    }

    /**
     * alias名がkeyStoreに登録されていることを精査する。
     * 登録されていない場合には、{@link IllegalArgumentException}を送出する。
     *
     * @throws KeyStoreException キーストアへのアクセスに失敗した場合
     */
    private void validateAlias() throws KeyStoreException {
        if (!keyStore.isKeyEntry(alias)) {
            throw new IllegalArgumentException(
                    "alias was not found in certificate file. alias name = [" + alias + "], fileName = ["
                            + certificateFileName + ']');
        }
    }
}


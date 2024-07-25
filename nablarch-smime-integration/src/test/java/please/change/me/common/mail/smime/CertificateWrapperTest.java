package please.change.me.common.mail.smime;


import java.security.cert.Certificate;
import org.junit.AfterClass;
import org.junit.Test;

import nablarch.core.repository.SystemRepository;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;

/**
 * {@link CertificateWrapper}のテストクラス。
 *
 * @author hisaaki sioiri
 */
public class CertificateWrapperTest {

    @AfterClass
    public static void afterClass() {
        SystemRepository.clear();
    }

    /** PKCS12形式のファイルから証明書、秘密鍵、公開鍵が取得できること。 */
    @Test
    public void testPKCS12Format() {
        CertificateWrapper wrapper = new CertificateWrapper();
        wrapper.setKeyStoreType("PKCS12");
        wrapper.setPassword("password");
        wrapper.setKeyPassword("password");
        wrapper.setCertificateFileName("classpath:please/change/me/common/mail/smime/data/pkcs12.p12");
        wrapper.initialize();
        Certificate[] certificates = wrapper.getCertificates();
        assertThat(certificates, is(notNullValue()));
        assertThat(certificates.length, is(4));
        for (Certificate certificate : certificates) {
            assertThat(certificate.getPublicKey(), is(notNullValue()));
            assertThat(certificate.getType(), is("X.509"));
        }
        assertThat(wrapper.getPrivateKey(), is(notNullValue()));

        Certificate rootCertificate = wrapper.getRootCertificate();
        assertThat("ルート証明書は、証明書チェーンの先頭の証明書と同一となる", rootCertificate, is(sameInstance(certificates[0])));
    }

    /** RSA形式(StoreType:JKS)形式のファイルから、証明書、秘密鍵、公開鍵が取得できること。 */
    @Test
    public void testRSAFormat() {
        CertificateWrapper wrapper = new CertificateWrapper();
        wrapper.setKeyStoreType("JKS");
        wrapper.setPassword("keystorePass");
        wrapper.setKeyPassword("testAliasPass");
        wrapper.setAlias("testAlias");
        wrapper.setCertificateFileName("classpath:please/change/me/common/mail/smime/data/test.dat");
        wrapper.initialize();
        Certificate[] certificates = wrapper.getCertificates();
        assertThat(certificates, is(notNullValue()));
        assertThat(certificates.length, is(1));
        assertThat(certificates[0].getPublicKey(), is(notNullValue()));
        assertThat(certificates[0].getType(), is("X.509"));

        Certificate rootCertificate = wrapper.getRootCertificate();
        assertThat("ルート証明書は、証明書チェーンの先頭の証明書と同一となる", rootCertificate, is(sameInstance(certificates[0])));
    }

    /** 証明書ファイルが存在しない場合。 */
    @Test
    public void testCertificateFileNotFound() {
        CertificateWrapper wrapper = new CertificateWrapper();
        wrapper.setKeyStoreType("JKS");
        wrapper.setPassword("keystorePass");
        wrapper.setKeyPassword("testAliasPass");
        wrapper.setAlias("testAlias");
        wrapper.setCertificateFileName("classpath:please/change/me/common/mail/smime/data/test.dat2");
        try {
            wrapper.initialize();
            fail("とおらない。");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(containsString("open failed.")));
            assertThat(e.getMessage(), is(containsString(
                    "url = [classpath:please/change/me/common/mail/smime/data/test.dat2]")));
        }
    }

    /**
     * 無効なalias名を指定した場合。
     */
    @Test
    public void testInvalidAlias() {
        CertificateWrapper wrapper = new CertificateWrapper();
        wrapper.setKeyStoreType("JKS");
        wrapper.setPassword("keystorePass");
        wrapper.setKeyPassword("testAliasPass");
        wrapper.setAlias("invalidAliasName");
        wrapper.setCertificateFileName("classpath:please/change/me/common/mail/smime/data/test.dat");
        try {
            wrapper.initialize();
            fail("とおらない。");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(containsString("alias was not found in certificate file.")));
            assertThat(e.getMessage(), is(containsString("fileName = [classpath:please/change/me/common/mail/smime/data/test.dat]")));
            assertThat(e.getMessage(), is(containsString("alias name = [invalidAliasName]")));
        }
    }

    /**
     * キーを持たない証明書を指定した場合でalias名を指定しなかった場合。
     */
    @Test
    public void testEmptyKeyCertificateFile() {
        CertificateWrapper wrapper = new CertificateWrapper();
        wrapper.setKeyStoreType("JKS");
        wrapper.setPassword("password");
        wrapper.setKeyPassword("password");
        wrapper.setCertificateFileName("classpath:please/change/me/common/mail/smime/data/EmptyKey.dat");
        try {
            wrapper.initialize();
            fail("とおらない。");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(containsString("key entry was not found in certificate file.")));
            assertThat(e.getMessage(), is(containsString("fileName = [classpath:please/change/me/common/mail/smime/data/EmptyKey.dat]")));
        }
    }

    /**
     * キーを持たない証明書を指定した場合でalias名を指定しなかった場合。
     */
    @Test
    public void testEmptyKeyCertificateFileSpecifyAlias() {
        CertificateWrapper wrapper = new CertificateWrapper();
        wrapper.setKeyStoreType("JKS");
        wrapper.setPassword("password");
        wrapper.setKeyPassword("password");
        wrapper.setCertificateFileName("classpath:please/change/me/common/mail/smime/data/EmptyKey.dat");
        try {
            wrapper.initialize();
            fail("とおらない。");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(containsString("key entry was not found in certificate file.")));
            assertThat(e.getMessage(), is(containsString("fileName = [classpath:please/change/me/common/mail/smime/data/EmptyKey.dat]")));
        }
    }

    /**
     * キーストアのパスワードが不正な場合。
     */
    @Test
    public void testInvalidKeyStorePassword() {
        CertificateWrapper wrapper = new CertificateWrapper();
        wrapper.setKeyStoreType("JKS");
        wrapper.setPassword("invalid");
        wrapper.setKeyPassword("password");
        wrapper.setAlias("alias");
        wrapper.setCertificateFileName("classpath:please/change/me/common/mail/smime/data/test.dat");
        try {
            wrapper.initialize();
            fail("とおらない。");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(containsString("Certificate file access error.")));
            assertThat(e.getMessage(), is(containsString("fileName = [classpath:please/change/me/common/mail/smime/data/test.dat]")));
        }
    }

    /**
     * エイリアスのパスワードが不正な場合。
     */
    @Test
    public void testInvalidAliasPassword() {
        CertificateWrapper wrapper = new CertificateWrapper();
        wrapper.setKeyStoreType("JKS");
        wrapper.setPassword("keystorePass");
        wrapper.setKeyPassword("invalidAliasPassword");
        wrapper.setAlias("testAlias");
        wrapper.setCertificateFileName("classpath:please/change/me/common/mail/smime/data/test.dat");
        try {
            wrapper.initialize();
            fail("とおらない。");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(containsString("Certificate file access error.")));
            assertThat(e.getMessage(), is(containsString("fileName = [classpath:please/change/me/common/mail/smime/data/test.dat]")));
        }
    }

    /**
     * 証明書として無効なファイル（空ファイル）を証明書として指定した場合。
     */
    @Test
    public void testInvalidFile() {
        CertificateWrapper wrapper = new CertificateWrapper();
        wrapper.setKeyStoreType("JKS");
        wrapper.setCertificateFileName("classpath:please/change/me/common/mail/smime/data/blank.txt");
        wrapper.setPassword("password");
        try {
            wrapper.initialize();
            fail("とおらない。");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(containsString("Certificate file access error.")));
            assertThat(e.getMessage(), is(containsString("fileName = [classpath:please/change/me/common/mail/smime/data/blank.txt]")));
        }
    }
}



package please.change.me.common.mail.smime;

import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.mail.smime.SMIMEUtil;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;

import nablarch.common.mail.MailAttachedFileTable;
import nablarch.common.mail.MailConfig;
import nablarch.common.mail.MailRequestTable;
import nablarch.common.mail.MailSender;
import nablarch.core.repository.SystemRepository;
import nablarch.fw.ExecutionContext;
import nablarch.fw.launcher.ProcessAbnormalEnd;

/**
 * 電子署名付きメール送信を行うバッチアクション。
 * <p/>
 * 本メール送信バッチアクションは、以下の手順でメール本文及び添付ファイルを元に電子署名を作成し、添付ファイルとして電子署名ファイルを付加する。
 * <ul>
 * <li>本メール送信機能は、{@link SystemRepository}(KEY:certificateList)から証明書リストを取得する。</li>
 * <li>バッチ起動時に指定されたメール送信パターンID(mailSendPatternId)に紐付く証明書({@link CertificateWrapper})を証明書リストより取得する。</li>
 * <li>取得した証明書({@link CertificateWrapper})を使用して電子署名を付加する</li>
 * </ul>
 *
 * @author hisaaki sioiri
 */
public class SMIMESignedMailSender extends MailSender {

    /** {@link SystemRepository}から証明書情報を取得するときのキー値 */
    private static final String CERTIFICATE_REPOSITORY_KEY = "certificateList";

    /**
     * {@inheritDoc}
     * <p/>
     * 本文（添付ファイル含む）の情報を元に電子署名を生成し、メール本文部を作成する。
     * 電子署名は、電子署名非対応のメーラーでも受信出来るように添付ファイル形式で付加する。
     */
    @Override
    protected void addBodyContent(MimeMessage mimeMessage, MailRequestTable.MailRequest mailRequest,
            List<? extends MailAttachedFileTable.MailAttachedFile> attachedFiles, ExecutionContext context) throws MessagingException {

        String mailSendPatternId = context.getSessionScopedVar("mailSendPatternId");
        Map<String, CertificateWrapper> certificateChain = SystemRepository.get(CERTIFICATE_REPOSITORY_KEY);
        CertificateWrapper certificateWrapper = certificateChain.get(mailSendPatternId);
        if (certificateWrapper == null) {
            throw createProcessAbnormalEnd(
                    new IllegalStateException(
                            String.format("No certification setting. mailSendPatternId=[%s]", mailSendPatternId)),
                    mailRequest);
        }

        try {
            // 電子署名を生成するジェネレータ
            SMIMESignedGenerator smimeSignedGenerator = new SMIMESignedGenerator();

            // 電子署名のジェネレータを設定
            smimeSignedGenerator.addSignerInfoGenerator(createSignerInfoGenerator(certificateWrapper));

            // 証明書の設定
            smimeSignedGenerator.addCertificates(createStore(certificateWrapper));

            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(mailRequest.getMailBody(), mailRequest.getCharset());

            if (attachedFiles.isEmpty()) {
                // 添付ファイルがない場合
                mimeMessage.setContent(smimeSignedGenerator.generate(bodyPart));
            } else {
                // 添付ファイルがある場合
                MimeMultipart multiPart = createMultiPart(attachedFiles);
                // 本文を先頭要素に追加する。
                multiPart.addBodyPart(bodyPart, 0);

                MimeBodyPart smimeBody = new MimeBodyPart();
                smimeBody.setContent(multiPart);
                mimeMessage.setContent(smimeSignedGenerator.generate(smimeBody));
            }
        } catch (OperatorCreationException e) {
            throw createProcessAbnormalEnd(e, mailRequest);
        } catch (CertificateEncodingException e) {
            throw createProcessAbnormalEnd(e, mailRequest);
        } catch (CertificateParsingException e) {
            throw createProcessAbnormalEnd(e, mailRequest);
        } catch (SMIMEException e) {
            throw createProcessAbnormalEnd(e, mailRequest);
        }
    }

    /**
     * 電子署名の設定失敗時のプロセス異常終了例外を生成する。
     *
     * @param e 元となる例外
     * @param mailRequest メール送信要求
     * @return {@link ProcessAbnormalEnd}
     */
    private ProcessAbnormalEnd createProcessAbnormalEnd(Exception e, MailRequestTable.MailRequest mailRequest) {
        MailConfig mailConfig = SystemRepository.get("mailConfig");
        return new ProcessAbnormalEnd(
                mailConfig.getAbnormalEndExitCode(), e,
                mailConfig.getSendFailureCode(), mailRequest.getMailRequestId());
    }

    /**
     * 添付ファイルを持つ{@link MimeMultipart}を生成する。
     * <p/>
     * 添付ファイルをBodyPartとして{@link MimeMultipart}に追加する。
     *
     *
     * @param attachedFiles 添付ファイルの情報
     * @return {@link MimeMultipart}
     * @throws MessagingException {@link MimeMultipart}の生成に失敗した場合
     */
    private MimeMultipart createMultiPart(List<? extends MailAttachedFileTable.MailAttachedFile> attachedFiles) throws MessagingException {
        MimeMultipart mimeMultipart = new MimeMultipart();
        for (MailAttachedFileTable.MailAttachedFile file : attachedFiles) {
            DataSource dataSource = new ByteArrayDataSource(
                    file.getFile(), file.getContextType());

            DataHandler dataHandler = new DataHandler(dataSource);
            MimeBodyPart filePart = new MimeBodyPart();
            filePart.setDataHandler(dataHandler);
            filePart.setFileName(file.getFileName());
            mimeMultipart.addBodyPart(filePart);
        }
        return mimeMultipart;
    }

    /**
     * 電子署名を生成するためのジェネレータを生成する。
     *
     * @param certificateWrapper 証明書情報
     * @return 電子署名を生成するためのジェネレータ
     * @throws OperatorCreationException ジェネレータの生成に失敗した場合
     * @throws CertificateEncodingException ジェネレータの生成に失敗した場合
     * @throws CertificateParsingException ジェネレータの生成に失敗した場合
     */
    private SignerInfoGenerator createSignerInfoGenerator(
            CertificateWrapper certificateWrapper) throws OperatorCreationException, CertificateEncodingException, CertificateParsingException {

        ASN1EncodableVector encodableVector = new ASN1EncodableVector();
        SMIMECapabilityVector capabilityVector = createCapabilityVector();
        encodableVector.add(new SMIMECapabilitiesAttribute(capabilityVector));

        // 証明書を元に発行元及びシリアル番号を取得
        X509Certificate rootCertificate = (X509Certificate) certificateWrapper.getRootCertificate();

        IssuerAndSerialNumber issuerAndSerialNumber = SMIMEUtil.createIssuerAndSerialNumberFor(rootCertificate);
        encodableVector.add(new SMIMEEncryptionKeyPreferenceAttribute(issuerAndSerialNumber));

        AttributeTable attributeTable = new AttributeTable(encodableVector);
        JcaSimpleSignerInfoGeneratorBuilder simpleSignerInfoGeneratorBuilder = new JcaSimpleSignerInfoGeneratorBuilder();
        simpleSignerInfoGeneratorBuilder.setSignedAttributeGenerator(attributeTable);

        return simpleSignerInfoGeneratorBuilder.build("SHA1withRSA", certificateWrapper.getPrivateKey(), rootCertificate);
    }

    /**
     * {@link SMIMECapabilityVector}を生成する。
     *
     * @return {@link SMIMECapabilityVector}
     */
    private SMIMECapabilityVector createCapabilityVector() {
        SMIMECapabilityVector vector = new SMIMECapabilityVector();
        vector.addCapability(SMIMECapability.aES256_CBC);
        vector.addCapability(SMIMECapability.dES_EDE3_CBC);
        vector.addCapability(SMIMECapability.rC2_CBC, 128);
        return vector;
    }

    /**
     * 証明書({@link X509Certificate})を元に{@link Store}を生成する。
     *
     * @param certificateWrapper 証明書
     * @return 指定された証明書を持つ {@link Store} オブジェクト
     * @throws CertificateEncodingException {@link Store}生成時に失敗した場合
     */
    private Store createStore(CertificateWrapper certificateWrapper) throws CertificateEncodingException {
        List<X509Certificate> certificateList = new ArrayList<X509Certificate>();
        Certificate[] certificates = certificateWrapper.getCertificates();
        for (Certificate certificate : certificates) {
            certificateList.add((X509Certificate) certificate);
        }
        return new JcaCertStore(certificateList);
    }
}


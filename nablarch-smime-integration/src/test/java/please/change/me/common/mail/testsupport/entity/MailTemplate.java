package please.change.me.common.mail.testsupport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * メールテンプレート
 */
@Entity
@IdClass(MailTemplate.MailTemplateId.class)
@Table(name = "MAIL_TEMPLATE")
public class MailTemplate {

    public MailTemplate() {
    }

    public MailTemplate(final String templateId, final String lang, final String subject, final String body,
            final String charset) {
        this.templateId = templateId;
        this.lang = lang;
        this.subject = subject;
        this.body = body;
        this.charset = charset;
    }

    @Id
    @Column(name = "TEMPLATE_ID", length = 3, nullable = false)
    public String templateId;

    @Id
    @Column(name = "LANG", length = 2, nullable = false)
    public String lang;

    @Column(name = "SUBJECT", length = 20)
    public String subject;

    @Column(name = "BODY", length = 1000)
    public String body;

    @Column(name = "CHARSET", length = 10)
    public String charset;

    /**
     * メールテンプレートの複合キー
     */
    @Embeddable
    public static class MailTemplateId {

        @Column(name = "TEMPLATE_ID", length = 3, nullable = false)
        public String templateId;

        @Column(name = "LANG", length = 2, nullable = false)
        public String lang;

    }
}

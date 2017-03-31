package please.change.me.common.mail.html.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * メールテンプレート
 */
@Entity
@IdClass(MailTemplate.MailTemplateId.class)
@Table(name = "MAIL_TEMPLATE")
public class MailTemplate {

    public MailTemplate() {
    }

    public MailTemplate(final String mailTemplateId, final String lang, final String subject, final String charset,
            final String mailBody) {
        this.mailTemplateId = mailTemplateId;
        this.lang = lang;
        this.subject = subject;
        this.charset = charset;
        this.mailBody = mailBody;
    }

    @Id
    @Column(name = "MAIL_TEMPLATE_ID", length = 10, nullable = false)
    public String mailTemplateId;

    @Id
    @Column(name = "LANG", length = 2, nullable = false)
    public String lang;

    @Column(name = "SUBJECT", length = 150, nullable = false)
    public String subject;

    @Column(name = "CHARSET", length = 50, nullable = false)
    public String charset;

    @Column(name = "MAIL_BODY", length = 1000, nullable = false)
    public String mailBody;

    public static class MailTemplateId {

        @Column(name = "MAIL_TEMPLATE_ID", length = 10, nullable = false)
        public String mailTemplateId;

        @Column(name = "LANG", length = 2, nullable = false)
        public String lang;
    }
}

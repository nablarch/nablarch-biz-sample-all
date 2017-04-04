package please.change.me.common.mail.html.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * メール代替テキストのテンプレート
 */
@Entity
@IdClass(AlternativeTextTemplate.AltanativeTextTemplateId.class)
@Table(name = "HTML_MAIL_ALT_TEXT_TEMPLATE")
public class AlternativeTextTemplate {

    public AlternativeTextTemplate() {
    }

    public AlternativeTextTemplate(final String mailTemplateId, final String lang, final String alternativeText) {
        this.mailTemplateId = mailTemplateId;
        this.lang = lang;
        this.alternativeText = alternativeText;
    }

    @Id
    @Column(name = "MAIL_TEMPLATE_ID", length = 10, nullable = false)
    public String mailTemplateId;

    @Id
    @Column(name = "LANG", length = 2, nullable = false)
    public String lang;

    @Column(name = "ALTERNATIVE_TEXT", length = 1000)
    public String alternativeText;

    public static class AltanativeTextTemplateId {

        @Column(name = "MAIL_TEMPLATE_ID", length = 10, nullable = false)
        public String mailTemplateId;

        @Column(name = "LANG", length = 2, nullable = false)
        public String lang;
    }
}

package please.change.me.common.mail.html.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * メール代替テキスト
 */
@Entity
@Table(name = "HTML_MAIL_ALT_TEXT")
public class AlternativeText {

    public AlternativeText() {
    }

    public AlternativeText(final String mailRequestId, final String alternativeText) {
        this.mailRequestId = mailRequestId;
        this.alternativeText = alternativeText;
    }

    @Id
    @Column(name = "MAIL_REQUEST_ID", length = 20, nullable = false)
    public String mailRequestId;

    @Column(name = "ALTERNATIVE_TEXT", length = 1000)
    public String alternativeText;
}

package please.change.me.common.mail.testsupport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * メッセージ
 */
@Entity
@IdClass(MailMessage.MailMassageId.class)
@Table(name = "MAIL_MESSAGE")
public class MailMessage {

    public MailMessage() {
    }

    public MailMessage(final String messageId, final String lang, final String message) {
        this.messageId = messageId;
        this.lang = lang;
        this.message = message;
    }

    @Id
    @Column(name = "MESSAGE_ID", length = 3, nullable = false)
    public String messageId;

    @Id
    @Column(name = "LANG", length = 2, nullable = false)
    public String lang;

    @Column(name = "MESSAGE", length = 250)
    public String message;

    /**
     * メールメッセージの複合キー
     */
    @Embeddable
    public static class MailMassageId {

        @Column(name = "MESSAGE_ID", length = 3, nullable = false)
        public String messageId;

        @Column(name = "LANG", length = 2, nullable = false)
        public String lang;

    }
}

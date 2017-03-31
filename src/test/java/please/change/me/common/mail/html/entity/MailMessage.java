package please.change.me.common.mail.html.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * メッセージ
 */
@Entity
@IdClass(MailMessage.MessageId.class)
@Table(name = "MESSAGE")
public class MailMessage {

    public MailMessage() {
    }

    public MailMessage(final String messageId, final String lang, final String message) {
        this.messageId = messageId;
        this.lang = lang;
        this.message = message;
    }

    @Id
    @Column(name = "MESSAGE_ID", length = 10, nullable = false)
    public String messageId;

    @Id
    @Column(name = "LANG", length = 2, nullable = false)
    public String lang;

    @Column(name = "MESSAGE", length = 200)
    public String message;

    public static class MessageId {

        @Column(name = "MESSAGE_ID", length = 10, nullable = false)
        public String messageId;

        @Column(name = "LANG", length = 2, nullable = false)
        public String lang;
    }
}

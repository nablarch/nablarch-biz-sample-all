package please.change.me.common.mail.html.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * メール送信先
 */
@Entity
@IdClass(MailRecipient.MailRecipientId.class)
@Table(name = "MAIL_RECIPIENT")
public class MailRecipient {

    public MailRecipient() {
    }

    public MailRecipient(final String mailRequestId, final Integer serialNumber, final String recipientType,
            final String mailAddress) {
        this.mailRequestId = mailRequestId;
        this.serialNumber = serialNumber;
        this.recipientType = recipientType;
        this.mailAddress = mailAddress;
    }

    @Id
    @Column(name = "MAIL_REQUEST_ID", length = 20, nullable = false)
    public String mailRequestId;

    @Id
    @Column(name = "SERIAL_NUMBER", precision = 10, nullable = false)
    public Integer serialNumber;

    @Column(name = "RECIPIENT_TYPE", length = 1, nullable = false)
    public String recipientType;

    @Column(name = "MAIL_ADDRESS", length = 100, nullable = false)
    public String mailAddress;

    public static class MailRecipientId {

        @Column(name = "MAIL_REQUEST_ID", length = 20, nullable = false)
        public String mailRequestId;

        @Column(name = "SERIAL_NUMBER", nullable = false)
        public Integer serialNumber;
    }
}

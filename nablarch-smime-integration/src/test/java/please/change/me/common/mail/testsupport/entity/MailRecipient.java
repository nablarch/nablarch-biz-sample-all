package please.change.me.common.mail.testsupport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * メール送信先
 */
@Entity
@IdClass(MailRecipient.MailRecipientId.class)
@Table(name = "MAIL_RECIPIENT_TABLE")
public class MailRecipient {

    public MailRecipient() {
    }

    @Id
    @Column(name = "MAIL_REQUEST_ID", length = 20, nullable = false)
    public String mailRequestId;

    @Id
    @Column(name = "RECIPIENT_NO", length=10, nullable = false)
    public Integer recipientNo;

    @Column(name = "RECIPIENT_TYPE", length = 3)
    public String recipientType;

    @Column(name = "MAIL_ADDRESS", length = 100)
    public String mailAddress;

    /**
     * メール送信先の複合キー
     */
    @Embeddable
    public static class MailRecipientId {

        @Column(name = "MAIL_REQUEST_ID", length = 20, nullable = false)
        public String mailRequestId;

        @Column(name = "RECIPIENT_NO", nullable = false)
        public Integer recipientNo;

    }
}

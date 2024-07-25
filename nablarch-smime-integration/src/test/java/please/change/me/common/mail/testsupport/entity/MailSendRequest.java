package please.change.me.common.mail.testsupport.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * メールリクエスト
 */
@Entity
@Table(name = "MAIL_SEND_REQUEST")
public class MailSendRequest {

    public MailSendRequest() {
    }

    @Id
    @Column(name = "MAIL_REQUEST_ID", length = 20, nullable = false)
    public String mailRequestId;

    @Column(name = "MAIL_SUBJECT", length = 100)
    public String mailSubject;

    @Column(name = "FROM_MAIL_ADDRESS", length = 100)
    public String fromMailAddress;

    @Column(name = "REPLY_MAIL_ADDRESS", length = 100)
    public String replyMailAddress;

    @Column(name = "RETURN_MAIL_ADDRESS", length = 100)
    public String returnMailAddress;

    @Column(name = "CHARSET", length = 200)
    public String charset;

    @Column(name = "STATUS", length = 1)
    public String status;

    @Column(name = "REQUEST_TIMESTAMP")
    public Timestamp requestTimestamp;

    @Column(name = "SENDING_TIMESTAMP")
    public Timestamp sendingTimestamp;

    @Column(name = "BODY", length = 4000)
    public String body;

    @Column(name = "MAIL_SEND_PATTERN_ID", length = 2)
    public String mailSendPatternId;
}

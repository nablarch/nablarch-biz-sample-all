package please.change.me.common.captcha.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * メッセージ
 */
@Entity
@Table(name = "MESSAGE")
public class CaptchaMessage {

    public CaptchaMessage() {
    }

    public CaptchaMessage(final String messageId, final String lang, final String message) {
        this.messageId = messageId;
        this.lang = lang;
        this.message = message;
    }

    @Id
    @Column(name = "MESSAGE_ID", length = 10, nullable = false)
    public String messageId;

    @Column(name = "LANG", length = 2)
    public String lang;

    @Column(name = "MESSAGE", length = 200)
    public String message;

}

package please.change.me.common.captcha.entity;


import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Captchaをデータベースに保存するエンティティ
 */
@Entity
@Table(name = "CAPTCHA_MANAGE")
public class CaptchaManage {

    public CaptchaManage() {
    }

    public CaptchaManage(final String captchaKey, final String captchaText, final Timestamp generateDateTime) {
        this.captchaKey = captchaKey;
        this.captchaText = captchaText;
        this.generateDateTime = generateDateTime;
    }

    @Id
    @Column(name = "CAPTCHA_KEY", length = 40, nullable = false)
    public String captchaKey;

    @Column(name = "CAPTCHA_TEXT", length = 10)
    public String captchaText;

    @Column(name = "GENERATE_DATE_TIME")
    public Timestamp generateDateTime;

}

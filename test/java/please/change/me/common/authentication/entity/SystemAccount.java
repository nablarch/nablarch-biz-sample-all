package please.change.me.common.authentication.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * システムアカウント
 */
@Entity
@Table(name = "SYSTEM_ACCOUNT")
public class SystemAccount {

    public SystemAccount() {
    }

    public SystemAccount(final String userId, final String password, final String userIdLocked,
            final String passwordExpirationDate,
            final Integer failedCount, final String effectiveDateFrom, final String effectiveDateTo,
            final Timestamp lastLoginDateTime) {
        this.userId = userId;
        this.password = password;
        this.userIdLocked = userIdLocked;
        this.passwordExpirationDate = passwordExpirationDate;
        this.failedCount = failedCount;
        this.effectiveDateFrom = effectiveDateFrom;
        this.effectiveDateTo = effectiveDateTo;
        this.lastLoginDateTime = lastLoginDateTime;
    }

    @Id
    @Column(name = "USER_ID", length = 10, nullable = false)
    public String userId;

    @Column(name = "PASSWORD", length = 128, nullable = false)
    public String password;

    @Column(name = "USER_ID_LOCKED", length = 1, nullable = false)
    public String userIdLocked = "0";

    @Column(name = "PASSWORD_EXPIRATION_DATE", length = 8, nullable = false)
    public String passwordExpirationDate = "99991231";

    @Column(name = "FAILED_COUNT", nullable = false)
    public Integer failedCount;

    @Column(name = "EFFECTIVE_DATE_FROM", length = 8, nullable = false)
    public String effectiveDateFrom = "19000101";

    @Column(name = "EFFECTIVE_DATE_TO", length = 8, nullable = false)
    public String effectiveDateTo = "99991231";

    @Column(name = "LAST_LOGIN_DATE_TIME")
    public Timestamp lastLoginDateTime;

}

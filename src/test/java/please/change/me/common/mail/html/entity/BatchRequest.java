package please.change.me.common.mail.html.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * バッチ要求
 */
@Entity
@Table(name = "BATCH_REQUEST")
public class BatchRequest {

    public BatchRequest() {
    }

    public BatchRequest(final String requestId, final String requestName, final String processHaltFlg,
            final String processActiveFlg,
            final String serviceAvailable) {
        this.requestId = requestId;
        this.requestName = requestName;
        this.processHaltFlg = processHaltFlg;
        this.processActiveFlg = processActiveFlg;
        this.serviceAvailable = serviceAvailable;
    }

    @Id
    @Column(name = "REQUEST_ID", length = 10, nullable = false)
    public String requestId;

    @Column(name = "REQUEST_NAME", length = 100, nullable = false)
    public String requestName;

    @Column(name = "PROCESS_HALT_FLG", length = 1, nullable = false)
    public String processHaltFlg;

    @Column(name = "PROCESS_ACTIVE_FLG", length = 1, nullable = false)
    public String processActiveFlg;

    @Column(name = "SERVICE_AVAILABLE", length = 1, nullable = false)
    public String serviceAvailable;
}

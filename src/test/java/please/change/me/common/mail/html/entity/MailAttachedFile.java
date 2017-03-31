package please.change.me.common.mail.html.entity;

import java.sql.Blob;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * メール添付ファイル
 */
@Entity
@IdClass(MailAttachedFile.MailAttachedFileId.class)
@Table(name = "MAIL_ATTACHED_FILE")
public class MailAttachedFile {

    public MailAttachedFile() {
    }

    public MailAttachedFile(final String mailRequestId, final Integer serialNumber,
            final String fileName, final String contentType, final Blob attachedFile) {
        this.mailRequestId = mailRequestId;
        this.serialNumber = serialNumber;
        this.fileName = fileName;
        this.contentType = contentType;
        this.attachedFile = attachedFile;
    }

    @Id
    @Column(name = "MAIL_REQUEST_ID", length = 20, nullable = false)
    public String mailRequestId;

    @Id
    @Column(name = "SERIAL_NUMBER", precision = 10)
    public Integer serialNumber;

    @Column(name = "FILE_NAME", length = 150, nullable = false)
    public String fileName;

    @Column(name = "CONTENT_TYPE", length = 50, nullable = false)
    public String contentType;

    @Column(name = "ATTACHED_FILE")
    public Blob attachedFile;

    public static class MailAttachedFileId {

        @Column(name = "MAIL_REQUEST_ID", length = 20, nullable = false)
        public String mailRequestId;

        @Column(name = "SERIAL_NUMBER", nullable = false)
        public Integer serialNumber;
    }
}

package please.change.me.common.mail.testsupport.entity;

import java.sql.Blob;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * メール添付ファイル
 */
@Entity
@IdClass(MailAttachedFile.MailAttachedFileId.class)
@Table(name = "MAIL_ATTACHED_FILE")
public class MailAttachedFile {

    public MailAttachedFile() {
    }

    @Id
    @Column(name = "MAIL_REQUEST_ID", length = 20, nullable = false)
    public String mailRequestId;

    @Id
    @Column(name = "ATTACHED_NO", length=10, nullable = false)
    public Integer attachedNo;

    @Column(name = "FILE_NAME", length = 100)
    public String fileName;

    @Column(name = "CONTENT_TYPE", length = 50)
    public String contentType;

    @Column(name = "ATTACHED_FILE")
    public Blob attachedFile;

    /**
     * メール添付ファイルの複合キー
     */
    @Embeddable
    public static class MailAttachedFileId {

        @Column(name = "MAIL_REQUEST_ID", length = 20, nullable = false)
        public String mailRequestId;

        @Column(name = "ATTACHED_NO", nullable = false)
        public Integer attachedNo;
    }
}

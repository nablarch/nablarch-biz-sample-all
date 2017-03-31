package please.change.me.common.file.management.entity;

import java.sql.Blob;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * ファイル管理のレコード
 */
@Entity
@Table(name="FILE_CONTROL")
public class FileControl {

    public FileControl() {
    }

    public FileControl(final String fileControlId, final Blob fileObject, final String sakujoSgn) {
        this.fileControlId = fileControlId;
        this.fileObject = fileObject;
        this.sakujoSgn = sakujoSgn;
    }

    @Id
    @Column(name="FILE_CONTROL_ID", length=18, nullable = false)
    public String fileControlId;

    @Column(name="FILE_OBJECT", nullable = false)
    public Blob fileObject;

    @Column(name="SAKUJO_SGN", length=1, nullable = false)
    public String sakujoSgn;
}

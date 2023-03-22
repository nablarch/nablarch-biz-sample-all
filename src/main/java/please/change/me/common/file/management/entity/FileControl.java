package please.change.me.common.file.management.entity;

import java.io.Serializable;
import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * FileControlエンティティクラス
 *
 */
@Generated("GSP")
@Entity
@Table(schema = "PUBLIC", name = "FILE_CONTROL")
public class FileControl implements Serializable {

    private static final long serialVersionUID = 1L;

    /** fileControlIdプロパティ */
    private String fileControlId;

    /** fileObjectプロパティ */
    private byte[] fileObject;

    /** sakujoSgnプロパティ */
    private String sakujoSgn;
    /**
     * fileControlIdを返します。
     *
     * @return fileControlId
     */
    @Id
    @Column(name = "FILE_CONTROL_ID", length = 18, nullable = false, unique = true)
    public String getFileControlId() {
        return fileControlId;
    }

    /**
     * fileControlIdを設定します。
     *
     * @param fileControlId
     */
    public void setFileControlId(String fileControlId) {
        this.fileControlId = fileControlId;
    }
    /**
     * fileObjectを返します。
     *
     * @return fileObject
     */
    @Lob
    @Column(name = "FILE_OBJECT", length = 2147483647, nullable = false, unique = false)
    public byte[] getFileObject() {
        return fileObject;
    }

    /**
     * fileObjectを設定します。
     *
     * @param fileObject
     */
    public void setFileObject(byte[] fileObject) {
        this.fileObject = fileObject;
    }
    /**
     * sakujoSgnを返します。
     *
     * @return sakujoSgn
     */
    @Column(name = "SAKUJO_SGN", length = 1, nullable = false, unique = false)
    public String getSakujoSgn() {
        return sakujoSgn;
    }

    /**
     * sakujoSgnを設定します。
     *
     * @param sakujoSgn
     */
    public void setSakujoSgn(String sakujoSgn) {
        this.sakujoSgn = sakujoSgn;
    }
}

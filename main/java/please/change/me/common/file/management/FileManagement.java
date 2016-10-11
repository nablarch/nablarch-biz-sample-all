package please.change.me.common.file.management;

import java.io.File;
import java.sql.Blob;

import nablarch.fw.web.upload.PartInfo;

/**
 * ファイル管理機能が実装すべきインターフェース。<br>
 *
 * @author Masaya Seko
 */
public interface FileManagement {
    /**
     * ファイルを保存する。
     * @param partInfo 保存対象のファイル
     * @return ファイル管理ID
     * @throws IllegalArgumentException ファイルが格納可能なファイル長を超えた場合及び、partInfoがnullの場合に送出される。
     */
    String save(PartInfo partInfo) throws IllegalArgumentException;

    /**
     * ファイルを保存する。
     * @param file 保存対象のファイル    
     * @return ファイル管理ID
     * @throws IllegalArgumentException ファイルが格納可能なファイル長を超えた場合及び、Fileオブジェクトがファイルを参照していなかった場合に送出される。
     */
    String save(File file) throws IllegalArgumentException;
    
    /**
     * ファイルを削除する。
     * @param fileId ファイル管理ID
     * @exception RuntimeException 削除対象のファイルが見つからなかった場合。
     */
    void delete(String fileId) throws RuntimeException;
    
    /**
     * ファイルを取得する。
     * @param fileId ファイル管理ID
     * @return ファイルのデータ。
     * @exception RuntimeException 指定したファイルが見つからなかった場合
     */
    Blob find(String fileId) throws RuntimeException;
}

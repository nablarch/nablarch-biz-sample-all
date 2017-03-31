package please.change.me.common.file.management;

import java.io.File;
import java.sql.Blob;

import nablarch.core.repository.SystemRepository;
import nablarch.fw.web.upload.PartInfo;

/**
 * DBでファイルを管理するユーティリティクラス。<br>
 * <p>
 * システムリポジトリから、fileManagementという名前で定義された{@link FileManagement}クラスのインスタンスを取得し、DBによるファイル管理機能を提供する。
 * </p>
 * @author Masaya Seko
 */
public final class FileManagementUtil {

    /**
     * 隠蔽コンストラクタ。
     */
    private FileManagementUtil() {
        
    }
    
    /**
     * ファイル管理機能のインスタンスをシステムリポジトリから取得する。
     * @return ファイル管理機能のインスタンス
     * @throws IllegalStateException システムリポジトリから、ファイル管理機能のインスタンスの取得に失敗した場合。
     */
    private static FileManagement getFileManagement() throws IllegalStateException {
        FileManagement fileManagement = SystemRepository.get("fileManagement");
        if (fileManagement == null) {
            throw new IllegalStateException("can't get fileManagement instance from System Repository."
                    + "check configuration. key=[fileManagement]");
        }
        return fileManagement;
    }
    
    /**
     * ファイルを保存する。
     * @param partInfo 保存対象のファイル
     * @return ファイル管理ID
     * @throws IllegalArgumentException ファイルが格納可能なファイル長を超えた場合及び、partInfoがnullの場合に送出される。
     */
    public static String save(PartInfo partInfo) throws IllegalArgumentException {
        return getFileManagement().save(partInfo);
    }

    /**
     * ファイルを保存する。
     * @param file 保存対象のファイル
     * @return ファイル管理ID
     * @throws IllegalArgumentException ファイルが格納可能なファイル長を超えた場合及び、Fileオブジェクトがファイルを参照していなかった場合に送出される。
     */
    public static String save(File file) throws IllegalArgumentException {
        return getFileManagement().save(file);
    }

    /**
     * ファイルを論理削除する。
     * @param fileId ファイル管理ID
     * @exception RuntimeException 削除対象のファイルが見つからなかった場合。
     */
    public static void delete(String fileId) throws RuntimeException {
        getFileManagement().delete(fileId);
    }
    
    /**
     * ファイルを取得する。
     * @param fileId ファイル管理ID
     * @return ファイルのデータ。
     * @exception RuntimeException 指定したファイルが見つからなかった場合
     */
    public static Blob find(String fileId) throws RuntimeException {
        return getFileManagement().find(fileId);
    }
}

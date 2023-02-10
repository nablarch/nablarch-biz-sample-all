package please.change.me.common.file.management;

import nablarch.common.dao.UniversalDao;
import nablarch.common.idgenerator.IdFormatter;
import nablarch.common.idgenerator.IdGenerator;
import nablarch.core.db.transaction.SimpleDbTransactionManager;
import nablarch.core.util.FileUtil;
import nablarch.fw.web.upload.PartInfo;
import please.change.me.common.file.management.entity.FileControl;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;
import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * DBを用いたファイル管理機能を行うクラス。<br>
 * <p>
 * DBでファイルを管理を行う処理を実装するクラス。<br>
 * DBによるファイル管理機能の利用者は本クラスは直接利用せず、{@link FileManagementUtil}クラスを利用する。
 * </p>
 *
 * @author Masaya Seko
 */
public class DbFileManagement implements FileManagement {

    /** データベースへのトランザクション制御を行う{@link SimpleDbTransactionManager} */
    private SimpleDbTransactionManager dbManager;

    /**格納可能なファイル長(単位：バイト)(デフォルトは10Mバイト)*/
    private int maxFileSize = 10000000;

    /**nablarchの採番機能で、DbFileManagement用の採番である旨を識別するためのKey*/
    private String fileIdKey;

    /**採番コンポーネント*/
    private IdGenerator idGenerator;
    
    /**採番IDを整形するためのフォーマッター*/
    private IdFormatter idFormatter;

    /**
     * データベースへのトランザクション制御を行う{@link SimpleDbTransactionManager}を設定する。
     *
     * @param dbManager データベースへのトランザクション制御を行う{@link SimpleDbTransactionManager}
     */
    public void setDbManager(SimpleDbTransactionManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * ファイルを保存する。
     * @param partInfo 保存対象のファイル
     * @return ファイル管理ID
     * @throws IllegalArgumentException ファイルが格納可能なファイル長を超えた場合及び、partInfoがnullの場合に送出される。
     */
    public String save(PartInfo partInfo) throws IllegalArgumentException {
        if (partInfo == null) {
           throw new IllegalArgumentException("partInfo is null.");
        }
        if (partInfo.size() > maxFileSize) {
            throw new IllegalArgumentException("File is too large. fileName = [" + partInfo.getFileName() + "]");
        }
        return save(partInfo.getInputStream(), partInfo.size());
    }

    /**
     * ファイルを保存する。
     * @param file 保存対象のファイル    
     * @return ファイル管理ID
     * @throws IllegalArgumentException ファイルが格納可能なファイル長を超えた場合及び、Fileオブジェクトがファイルを参照していなかった場合に送出される。
     */
    public String save(File file) throws IllegalArgumentException {
        if (file == null) {
            throw new IllegalArgumentException("file is null.");
         }
        if (file.length() > maxFileSize) {
            throw new IllegalArgumentException("File is too large. fileName = [" + file.getName() + "]");
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            return save(fileInputStream, (int) file.length());
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        } finally {
            FileUtil.closeQuietly(fileInputStream);
        }
    }

    /**
     * ファイルを保存する。
     * @param inStream DBに保存するファイルの内容。
     * @param fileSize 保存するファイルのサイズ。
     * @return ファイル管理ID
     */
    private String save(InputStream inStream, int fileSize) {
        BufferedInputStream bufferedInStream = new BufferedInputStream(inStream);
        byte[] bytes = new byte[fileSize];

        try {
            bufferedInStream.read(bytes);
            String fileControlId = idGenerator.generateId(fileIdKey, idFormatter);
            FileControl fileControl = new FileControl();
            fileControl.setFileControlId(fileControlId);
            fileControl.setFileObject(bytes);;
            fileControl.setSakujoSgn("0");
            UniversalDao.insert(fileControl);
            return fileControlId;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtil.closeQuietly(bufferedInStream);
        }
    }

    /**
     * ファイルを論理削除する。
     * @param fileControlId ファイル管理ID
     * @exception RuntimeException 削除対象のファイルが見つからなかった場合。
     */
    public void delete(String fileControlId) {
        FileControl fileControl =  UniversalDao.findBySqlFile(FileControl.class, "SELECT_FILE_CONTROL", new Object[]{fileControlId});
        fileControl.setSakujoSgn("1");
        UniversalDao.update(fileControl);
    }

    /**
     * ファイルを取得する。
     * @param fileControlId ファイル管理ID
     * @return ファイルのデータ。
     * @exception RuntimeException 指定したファイルが見つからなかった場合
     */
    public Blob find(String fileControlId) {
        FileControl fileControl =  UniversalDao.findBySqlFile(FileControl.class, "SELECT_FILE_CONTROL", new Object[]{fileControlId});

        byte[] bytes = fileControl.getFileObject();
        try {
            return new SerialBlob(bytes);
        } catch (SerialException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * DBへの格納を許可するファイルの最大サイズ(単位：バイト)を設定する。
     * @param maxFileSize DBへの格納を許可するファイルの最大サイズ
     */
    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
     * 採番コンポーネントを設定する。
     * @param idGenerator 採番コンポーネント
     */
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * 採番IDを整形するためのフォーマッターを設定する。
     * @param idFormatter フォーマッター
     */
    public void setIdFormatter(IdFormatter idFormatter) {
        this.idFormatter = idFormatter;
    }

    /**
     * nablarchの採番機能用IDを設定する(nablarchの採番機能用)。
     * @param fileIdKey nablarchの採番機能で採番対象を識別するためのID
     */
    public void setFileIdKey(String fileIdKey) {
        this.fileIdKey = fileIdKey;
    }
}

package please.change.me.common.file.management;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Blob;

import nablarch.common.idgenerator.IdFormatter;
import nablarch.common.idgenerator.IdGenerator;
import nablarch.core.db.statement.SqlPStatement;
import nablarch.core.db.statement.SqlResultSet;
import nablarch.core.db.statement.SqlRow;
import nablarch.core.db.support.DbAccessSupport;
import nablarch.core.util.FileUtil;
import nablarch.fw.web.upload.PartInfo;

/**
 * DBを用いたファイル管理機能を行うクラス。<br>
 * <p>
 * DBでファイルを管理を行う処理を実装するクラス。<br>
 * DBによるファイル管理機能の利用者は本クラスは直接利用せず、{@link FileManagementUtil}クラスを利用する。
 * </p>
 *
 * @author Masaya Seko
 */
public class DbFileManagement extends DbAccessSupport implements FileManagement {
    /**格納可能なファイル長(単位：バイト)(デフォルトは10Mバイト)*/
    private int maxFileSize = 10000000;

    /**nablarchの採番機能で、DbFileManagement用の採番である旨を識別するためのKey*/
    private String fileIdKey;

    /**採番コンポーネント*/
    private IdGenerator idGenerator;
    
    /**採番IDを整形するためのフォーマッター*/
    private IdFormatter idFormatter;

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
        BufferedInputStream bufferedInStream = null;
        try {
            bufferedInStream = new BufferedInputStream(inStream);
            String fileId = idGenerator.generateId(fileIdKey, idFormatter);

            SqlPStatement stmt = getSqlPStatement("INSERT_FILE_CONTROL");
            stmt.setString(1, fileId);
            stmt.setBinaryStream(2, bufferedInStream, fileSize);
            stmt.executeUpdate();
            return fileId;
        } finally {
            FileUtil.closeQuietly(bufferedInStream);
        }
    }

    /**
     * ファイルを論理削除する。
     * @param fileId ファイル管理ID
     * @exception RuntimeException 削除対象のファイルが見つからなかった場合。
     */
    public void delete(String fileId) throws RuntimeException {
        SqlPStatement stmt = getSqlPStatement("DELETE_FILE_CONTROL");
        stmt.setString(1, fileId);
        int count = stmt.executeUpdate();
        if (count != 1) {
            throw new RuntimeException("Record not found. FILE_CONTROL_ID= [" + fileId + "]");
        }
    }
    
    /**
     * ファイルを取得する。
     * @param fileId ファイル管理ID
     * @return ファイルのデータ。
     * @exception RuntimeException 指定したファイルが見つからなかった場合
     */
    public Blob find(String fileId) throws RuntimeException {
        SqlPStatement stmt = getSqlPStatement("SELECT_FILE_CONTROL");
        stmt.setString(1, fileId);
        SqlResultSet resultSet = stmt.retrieve();
        if (resultSet.size() == 1) {
            SqlRow sqlRow = resultSet.get(0);
            return (Blob) sqlRow.get("FILE_OBJECT");
        } else {
            //取得結果が0件の場合
            throw new RuntimeException("Record not found. FILE_CONTROL_ID= [" + fileId + "]");
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

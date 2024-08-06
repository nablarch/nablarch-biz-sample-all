package please.change.me.common.file.management;

import nablarch.common.idgenerator.IdFormatter;
import nablarch.common.idgenerator.IdGenerator;
import nablarch.core.db.connection.ConnectionFactory;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.util.BinaryUtil;
import nablarch.core.util.FileUtil;
import nablarch.fw.web.upload.PartInfo;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

import javax.sql.rowset.serial.SerialBlob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DbFileManagementTest {

    /**
     * テストデータなどをセットアップするためのコネクション
     */
    private static Connection con;

    /** xml（テスト用設定ファイル）の配置ディレクトリ */
    private static final String COMPONENT_BASE_PATH = "please/change/me/common/file/management/";

    /**
     * セットアップ。
     *
     * テスト時に使用するデータベース接続の生成及びテスト用のテーブルのセットアップを行う。
     *
     * @throws SQLException 例外
     */
    @BeforeAll
    static void classSetup() throws SQLException {

        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:~/nablarch_test");
        ds.setUser("sa");
        ds.setPassword("");
        con = ds.getConnection();

        // setup test table
        Statement statement = con.createStatement();
        statement.execute("DROP TABLE IF EXISTS FILE_CONTROL CASCADE CONSTRAINTS");

        statement.execute("CREATE TABLE FILE_CONTROL("
                + " FILE_CONTROL_ID CHAR(18) NOT NULL,"
                + " FILE_OBJECT BLOB NOT NULL,"
                + " SAKUJO_SGN CHAR(1) NOT NULL,"
                + " PRIMARY KEY (FILE_CONTROL_ID))"
        );

        statement.close();
    }

    @BeforeEach
    void setUp() throws Exception {

        DbConnectionContext.removeConnection();

        PreparedStatement truncate = con.prepareStatement("truncate table FILE_CONTROL");
        truncate.execute();
        truncate.close();

        // テストデータのセットアップ
        // 有効なレコード
        PreparedStatement insert = con.prepareStatement("insert into FILE_CONTROL values (?, ?, ?)");
        insert.setString(1, "900000000000000001");
        insert.setBlob(2, new SerialBlob("abc".getBytes("utf-8")));
        insert.setString(3, "0");
        insert.execute();

        // 論理削除済みレコード
        insert.setString(1, "900000000000000002");
        insert.setBlob(2, new SerialBlob("def".getBytes("utf-8")));
        insert.setString(3, "1");
        insert.execute();

        insert.close();
        con.commit();

        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                COMPONENT_BASE_PATH + "dbFileManagement.xml");
        SystemRepository.load(new DiContainer(loader));

        ConnectionFactory factory = SystemRepository.get("connectionFactory");
        DbConnectionContext.setConnection("transaction", factory.getConnection("transaction"));
    }

    @AfterEach
    void tearDown() {
        DbConnectionContext.getTransactionManagerConnection("transaction").commit();
        DbConnectionContext.removeConnection();
    }

    /**
     * クラス終了時の処理。
     *
     * @throws Exception 例外
     */
    @AfterAll
    static void classDown() throws Exception {
        if (con != null) {
            con.close();
        }
    }

    /**
     * 有効なレコードに対する取得テスト。
     * @throws Exception
     */
    @Test
    public void testFindExist() throws Exception {
        Blob blob = FileManagementUtil.find("900000000000000001");
        byte[] byteArray = BinaryUtil.toByteArray(blob.getBinaryStream());

        // 存在するファイル管理IDを指定した場合は、ファイルの内容を取得できる。
        assertThat(new String(byteArray, "UTF-8"), is("abc"));
    }

    /**
     * 有効なレコードに対するデータ削除のテスト。<br>
     * 
     * データ削除後、findでレコードが取得できずに例外が送出されることによって、レコードの削除を確認する。
     */
    @Test
    public void testDeleteNormal() {
        // 存在するファイル管理IDを指定した場合は、レコードを削除できることを確認する。
        String fileControlId = "900000000000000001";
        FileManagementUtil.delete(fileControlId);

        // 削除済みのため例外が発生する。
        assertThrows(RuntimeException.class, () -> FileManagementUtil.find(fileControlId));
    }

    /**
     * 論理削除済みデータを削除しようとした際に、例外が送出されるテスト。
     */
    @Test
    public void testDeleteDeleted() {
        // 論理削除されたデータの場合、実行時例外が送出される。
        String fileControlId = "900000000000000002";
        assertThrows(RuntimeException.class, () -> FileManagementUtil.delete(fileControlId));
    }

    /**
     * 存在しないレコードを削除しようとした際に、例外が送出されるテスト。
     */
    @Test
    public void testDelete() {
        // 該当するファイル管理IDが存在しない場合、実行時例外が送出される。
        String fileControlId = "900000000000000003";
        assertThrows(RuntimeException.class, () -> FileManagementUtil.delete(fileControlId));
    }

    /**
     * Fileオブジェクト登録のテスト(正常系)。
     * @throws Exception
     */
    @Test
    public void testSaveFileNormal() throws Exception {
        // ファイル登録のテストに使用するファイルを作成。
        File uploadFile = prepareUploadFile("abcde");
        
        // Fileオブジェクトを登録する。
        MockIdGenerator.setReturnId("4");
        String fileControlId = FileManagementUtil.save(uploadFile);
        assertThat(fileControlId, is("000000000000000004"));

        // 登録したオブジェクトを取り出せるか確認する。
        Blob blob = FileManagementUtil.find(fileControlId);
        byte[] byteArray = BinaryUtil.toByteArray(blob.getBinaryStream());
        assertThat(new String(byteArray, "UTF-8"), is("abcde"));
    }

    /**
     * PartInfoオブジェクトデータ登録のテスト(正常系)。
     * @throws Exception
     */
    @Test
    public void testSavePartInfoNormal() throws Exception {
        // ファイル登録のテストに使用するファイルを作成。
        File uploadFile = prepareUploadFile("abcde");
        
        // PartInfoを登録する。
        PartInfo partInfo = PartInfo.newInstance("dbFileManagement.txt");
        partInfo.setSavedFile(uploadFile);
        partInfo.setSize(5);

        MockIdGenerator.setReturnId("1");
        String fileId = FileManagementUtil.save(partInfo);
        assertThat(fileId, is("000000000000000001"));
        // 登録したオブジェクトを取り出せるか確認する。
        InputStream inputStream = null;
        Blob blob = FileManagementUtil.find(fileId);
        byte[] byteArray = BinaryUtil.toByteArray(blob.getBinaryStream());
        assertThat(new String(byteArray, "UTF-8"), is("abcde"));
    }

    /**
     * ディレクトリを登録しようとした場合に例外が送出されるテスト。
     */
    @Test
    public void testSaveDirectory() {
        File directory = new File(".");
        assertThrows(IllegalArgumentException.class, () -> FileManagementUtil.save(directory));
    }

    /**
     * nullのFileを登録しようとした際に、例外が送出されるテスト。
     * @throws Exception
     */
    @Test
    public void testSaveNullFile() {
        File nullFile = null;
        assertThrows(IllegalArgumentException.class, () -> FileManagementUtil.save(nullFile));
    }

    /**
     * nullのpartInfoを登録しようとした際に、例外が送出されるテスト。
     * @throws Exception
     */
    @Test
    public void testSaveIllegal() {
        PartInfo nullPartInfo = null;
        assertThrows(IllegalArgumentException.class, () -> FileManagementUtil.save(nullPartInfo));
    }

    /**
     * 制限サイズと同じサイズの際に、Fileが登録できることを確認するテスト。
     * @throws Exception
     */
    @Test
    public void testSaveFileSizeNormal() throws Exception {
        DbFileManagement dbFileManagement = SystemRepository.get("fileManagement");
        // テストのためにファイルサイズを小さくする。
        dbFileManagement.setMaxFileSize(1);
        
        // 登録可能な最大サイズ
        File oneByteFile = prepareUploadFile("a");
        MockIdGenerator.setReturnId("2");
        String fileId = dbFileManagement.save(oneByteFile);
        assertThat(fileId, is("000000000000000002"));
    }

    /**
     * 制限サイズよりファイルが大きい際に、File登録に失敗することを確認するテスト。
     * @throws Exception
     */
    @Test
    public void testSaveFileSizeIllegal() throws Exception {
        DbFileManagement dbFileManagement = SystemRepository.get("fileManagement");
        // テストのためにファイルサイズを小さくする。
        dbFileManagement.setMaxFileSize(1);

        // 登録可能な最大サイズを1超えた場合
        File twoByteFile = prepareUploadFile("ab");
        assertThrows(IllegalArgumentException.class, () -> dbFileManagement.save(twoByteFile));
    }

    /**
     * 制限サイズと同じサイズの際に、PartInfoが登録できることを確認するテスト。
     * @throws Exception
     */
    @Test
    public void testSavePartInfoSizeNormal() throws Exception {
        DbFileManagement dbFileManagement = SystemRepository.get("fileManagement");
        //テストのためにファイルサイズを小さくする。
        dbFileManagement.setMaxFileSize(1);
        
        //登録可能な最大サイズ
        File oneByteFile = prepareUploadFile("a");
        PartInfo partInfo = PartInfo.newInstance(oneByteFile.getName());
        MockIdGenerator.setReturnId("3");
        partInfo.setSavedFile(oneByteFile);
        partInfo.setSize((int) oneByteFile.length());
        
        String fileId = dbFileManagement.save(partInfo);
        assertThat(fileId, is("000000000000000003"));
    }

    /**
     * 制限サイズと同じサイズの際に、PartInfoが登録できることを確認するテスト。
     * @throws Exception
     */
    @Test
    public void testSavePartInfoSizeIllegal() throws Exception {
        DbFileManagement dbFileManagement = SystemRepository.get("fileManagement");
        //テストのためにファイルサイズを小さくする。
        dbFileManagement.setMaxFileSize(1);
        
        //登録可能な最大サイズ
        //登録可能な最大サイズを1超えた場合
        File twoByteFile = prepareUploadFile("ab");
        PartInfo partInfo = PartInfo.newInstance(twoByteFile.getName());
        partInfo.setSavedFile(twoByteFile);
        partInfo.setSize((int) twoByteFile.length());

        assertThrows(RuntimeException.class, () -> dbFileManagement.save(partInfo));
    }

    /**
     * コンポーネント定義が行われていない場合のテスト。
     */
    @Test
    public void testUninitialized() {
        SystemRepository.clear();
        assertThrows(IllegalStateException.class, () -> FileManagementUtil.find("900000000000000001"));
    }

    /**
     * アップロードのテストに使用するファイルを作成する。
     * @param data ファイルの内容
     * @return Fileオブジェクト
     */
    private File prepareUploadFile(String data) throws Exception {
        File uploadFile = File.createTempFile("DbFileManagement", ".txt");
        uploadFile.deleteOnExit();
        FileOutputStream fileOutputStream = null;
        try{
            fileOutputStream = new FileOutputStream(uploadFile);
            fileOutputStream.write(data.getBytes("UTF-8"));
        }finally{
            FileUtil.closeQuietly(fileOutputStream);
        }
        return uploadFile;
    }

    /**
     * 常に固定の値を返すテスト用採番機能
     * @author Masaya Seko
     *
     */
    public static class MockIdGenerator implements IdGenerator {

        /**生成するID*/
        public static String returnId;
        
        
        @Override
        public String generateId(String id) {
            return returnId;
        }

        @Override
        public String generateId(String id, IdFormatter formatter) {
            return formatter.format(id, generateId(id));
        }

        /**
         * 生成するIDを設定する。<br>
         * 
         * このメソッドで設定した値を常にIDとして返却する。
         * @param argReturnId 生成するID
         */
        public static void setReturnId(String argReturnId) {
            returnId = argReturnId;
        }
    }
}

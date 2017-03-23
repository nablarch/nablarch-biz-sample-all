package please.change.me.common.file.management;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Blob;

import javax.sql.rowset.serial.SerialBlob;

import please.change.me.common.file.management.entity.FileControl;

import nablarch.common.idgenerator.IdFormatter;
import nablarch.common.idgenerator.IdGenerator;
import nablarch.core.db.connection.ConnectionFactory;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.connection.TransactionManagerConnection;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.util.BinaryUtil;
import nablarch.core.util.FileUtil;
import nablarch.fw.web.upload.PartInfo;
import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.db.helper.DatabaseTestRunner;
import nablarch.test.support.db.helper.VariousDbTestHelper;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DatabaseTestRunner.class)
public class DbFileManagementTest {
    /**テスト対象が使用するコネクション*/
    static TransactionManagerConnection tmConn;

    @Rule
    public SystemRepositoryResource resouce = new SystemRepositoryResource(
            "please/change/me/common/file/management/dbFileManagement.xml");
    /**
     * セットアップ。
     *
     * テスト時に使用するデータベース接続の生成及びテスト用のテーブルのセットアップを行う。
     *
     */
    @BeforeClass
    public static void classSetup() {
        // setup test table
        VariousDbTestHelper.createTable(FileControl.class);

        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader("please/change/me/common/file/management/dbFileManagement.xml");
        SystemRepository.load(new DiContainer(loader));
        
        ConnectionFactory factory = SystemRepository.get("connectionFactory");
        tmConn = factory.getConnection("test");
    }

    /**
     * クラス終了時の処理。
     *
     * @throws Exception 例外
     */
    @AfterClass
    public static void classDown() throws Exception {
        // drop table
        VariousDbTestHelper.dropTable(FileControl.class);

        SystemRepository.clear();
    }
    
    @Before
    public void setUp(){
        DbConnectionContext.setConnection(tmConn);
    }
    
    @After
    public void tearDown(){
        tmConn.rollback();
        DbConnectionContext.removeConnection();
    }
    
    /**
     * 有効なレコードに対する取得テスト。
     * @throws Exception
     */
    @Test
    public void testFindExist() throws Exception {
        prepareDbData();

        //存在するファイル管理IDを指定した場合は、ファイルの内容を取得できる。
        InputStream inputStream = null;
        try{
            Blob blob = FileManagementUtil.find("900000000000000001");
            byte[] byteArray = BinaryUtil.toByteArray(blob.getBinaryStream());
            assertThat(new String(byteArray, "UTF-8"), is("abc"));
        } finally {
            FileUtil.closeQuietly(inputStream);
        }
    }

    /**
     * 論理削除済みデータを取得しようとすると、例外が送出されるテスト。
     * @throws Exception
     */
    @Test(expected = RuntimeException.class)
    public void testFindDeleted() throws Exception {
        prepareDbData();

        FileManagementUtil.find("900000000000000002");
    }

    /**
     * 該当するファイル管理IDが存在しない際にデータ取得をすると、例外が送出されるテスト。
     * @throws Exception
     */
    @Test(expected = RuntimeException.class)
    public void testFind() throws Exception {
        prepareDbData();

        FileManagementUtil.find("900000000000000003");
    }

    /**
     * 有効なレコードに対するデータ削除のテスト。<br>
     * 
     * データ削除後、findでレコードが取得できずに例外が送出されることによって、レコードの削除を確認する。
     * @throws Exception
     */
    @Test
    public void testDeleteNormal() throws Exception {
        prepareDbData();
        
        //存在するファイル管理IDを指定した場合は、レコードを削除できることを確認する。
        
        String fileId = "900000000000000001";
        FileManagementUtil.delete(fileId);
    }

    /**
     * 論理削除済みデータを削除しようとした際に、例外が送出されるテスト。
     * @throws Exception
     */
    @Test(expected = RuntimeException.class)
    public void testDeleteDeleted() throws Exception {
        prepareDbData();

        //論理削除されたデータの場合、実行時例外が送出される。
        String fileId = "900000000000000002";
        FileManagementUtil.delete(fileId);
    }

    /**
     * 存在しないレコードを削除しようとした際に、例外が送出されるテスト。
     * @throws Exception
     */
    @Test(expected = RuntimeException.class)
    public void testDelete() throws Exception {
        prepareDbData();

        //該当するファイル管理IDが存在しない場合、実行時例外が送出される。
        String fileId = "900000000000000003";
        FileManagementUtil.delete(fileId);
    }


    /**
     * Fileオブジェクト登録のテスト(正常系)。
     * @throws Exception
     */
    @Test
    public void testSaveFileNormal() throws Exception {
        //ファイル登録のテストに使用するファイルを作成。
        File uploadFile = prepareUploadFile("abcde");
        
        //Fileオブジェクトを登録する。
        MockIdGenerator.setReturnId("4");
        String fileId = FileManagementUtil.save(uploadFile);
        assertThat(fileId, is("000000000000000004"));

        //登録したオブジェクトを取り出せるか確認する。
        InputStream inputStream = null;
        try{
            Blob blob = FileManagementUtil.find(fileId);
            byte[] byteArray = BinaryUtil.toByteArray(blob.getBinaryStream());
            assertThat(new String(byteArray, "UTF-8"), is("abcde"));
        }finally{
            FileUtil.closeQuietly(inputStream);
        }
    }

    /**
     * PartInfoオブジェクトデータ登録のテスト(正常系)。
     * @throws Exception
     */
    @Test
    public void testSavePartInfoNormal() throws Exception {
        //ファイル登録のテストに使用するファイルを作成。
        File uploadFile = prepareUploadFile("abcde");
        
        //PartInfoを登録する。
        PartInfo partInfo = PartInfo.newInstance("dbFileManagement.txt");
        partInfo.setSavedFile(uploadFile);
        partInfo.setSize(5);
        MockIdGenerator.setReturnId("1");
        String fileId = FileManagementUtil.save(partInfo);
        assertThat(fileId, is("000000000000000001"));
        //登録したオブジェクトを取り出せるか確認する。
        InputStream inputStream = null;
        try{
            Blob blob = FileManagementUtil.find(fileId);
            byte[] byteArray = BinaryUtil.toByteArray(blob.getBinaryStream());
            assertThat(new String(byteArray, "UTF-8"), is("abcde"));
        } finally {
            FileUtil.closeQuietly(inputStream);
        }
    }

    /**
     * ディレクトリを登録しようとした場合に例外が送出されるテスト。
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSaveDirectory() throws Exception {
        File directory = new File(".");
        //ファイル以外(ここではディレクトリ)を登録しようとした場合。
        FileManagementUtil.save(directory);
    }

    /**
     * nullのFileを登録しようとした際に、例外が送出されるテスト。
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSaveNullFile() throws Exception {
        //nullのFileを登録しようとした場合
        File file = null;
        FileManagementUtil.save(file);
    }

    /**
     * nullのpartInfoを登録しようとした際に、例外が送出されるテスト。
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSaveIllegal() throws Exception {
        //nullのPartInfoを登録しようとした場合
        PartInfo partInfo = null;
        FileManagementUtil.save(partInfo);
    }

    /**
     * 制限サイズと同じサイズの際に、Fileが登録できることを確認するテスト。
     * @throws Exception
     */
    @Test
    public void testSaveFileSizeNormal() throws Exception {
        DbFileManagement dbFileManagement = SystemRepository.get("fileManagement");
        //テストのためにファイルサイズを小さくする。
        dbFileManagement.setMaxFileSize(1);
        
        //登録可能な最大サイズ
        File oneByteFile = prepareUploadFile("a");
        MockIdGenerator.setReturnId("2");
        String fileId = dbFileManagement.save(oneByteFile);
        assertThat(fileId, is("000000000000000002"));
    }

    /**
     * 制限サイズよりファイルが大きい際に、File登録に失敗することを確認するテスト。
     * @throws Exception
     */
    @Test(expected = RuntimeException.class)
    public void testSaveFileSizeIllegal() throws Exception {
        DbFileManagement dbFileManagement = SystemRepository.get("fileManagement");
        //テストのためにファイルサイズを小さくする。
        dbFileManagement.setMaxFileSize(1);
        
        //登録可能な最大サイズを1超えた場合
        File twoByteFile = prepareUploadFile("ab");
        dbFileManagement.save(twoByteFile);
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
    @Test(expected = RuntimeException.class)
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
        
        dbFileManagement.save(partInfo);
    }

    /**
     * コンポーネント定義が行われていない場合のテスト。
     * @throws Exception
     */
    @Test(expected = IllegalStateException.class)
    public void testUninitialized() throws Exception{
        SystemRepository.clear();
        FileManagementUtil.find("900000000000000001");
    }

    /**
     * 正常系用のDBテストデータを用意する処理。
     */
    private void prepareDbData() throws Exception {
        Blob blob1 = new SerialBlob("abc".getBytes("utf-8"));
        Blob blob2 = new SerialBlob("def".getBytes("utf-8"));
        VariousDbTestHelper.setUpTable(
                //有効なレコード
                new FileControl("900000000000000001", blob1, "0"),
                //論理削除済みレコード
                new FileControl("900000000000000002", blob2, "1")
        );
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

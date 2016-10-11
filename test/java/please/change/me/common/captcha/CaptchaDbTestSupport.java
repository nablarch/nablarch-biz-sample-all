package please.change.me.common.captcha;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ResourceBundle;

import nablarch.core.db.connection.ConnectionFactory;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.connection.TransactionManagerConnection;
import nablarch.core.repository.SystemRepository;
import oracle.jdbc.pool.OracleDataSource;

/**
 * DBがカラムテストのサポートクラス
 * 
 * @author TIS
 */
public class CaptchaDbTestSupport {

    /**テスト対象が使用するコネクション*/
    private static TransactionManagerConnection tmConn;
    
    /** テストデータのセットアップや検証を行うためのコネクション */
    private static Connection con;

    /**
     * DB関係セットアップ。
     *
     * テスト時に使用するデータベース接続の生成及びテスト用のテーブルのセットアップを行う。
     *
     * @throws SQLException 例外
     */
    protected static void setupDb() throws SQLException {
        initTestConnection();
        dropManageTable();
        createManageTable();
        
        dropMessageTable();
        createMessageTable();
        insertIntoMessageTable("MSG90001", "ja", "{0}が正しくありません。");
        commitTestTran();
        
        ConnectionFactory factory = SystemRepository.get("connectionFactory");
        tmConn = factory.getConnection("test");
        DbConnectionContext.setConnection(tmConn);
    }
    
    /**
     * DB関係終了処理。
     *
     * @throws Exception 例外
     */
    protected static void teardownDb() throws Exception {
        DbConnectionContext.removeConnection();
        if (con != null) {
            con.close();
        }
    }

    /**
     * テスト用コネクションの初期化
     * 
     * @throws SQLException 例外
     */
    protected static void initTestConnection() throws SQLException {
        ResourceBundle rb = ResourceBundle.getBundle("db-config");
        OracleDataSource ds = new OracleDataSource();
        ds.setURL(rb.getString("db.url"));
        ds.setUser(rb.getString("db.user"));
        ds.setPassword(rb.getString("db.password"));
        con = ds.getConnection();
        con.setAutoCommit(false);
    }
    
    /**
     * 管理テーブルの作成
     * 
     * @throws SQLException 例外
     */
    protected static void createManageTable() throws SQLException {
        Statement statement = con.createStatement();
        try {
            statement.execute("CREATE TABLE CAPTCHA_MANAGE (CAPTCHA_KEY VARCHAR2(40) PRIMARY KEY, CAPTCHA_TEXT VARCHAR2(10), GENERATE_DATE_TIME TIMESTAMP)");
        } catch (Exception e) {
            // nop
        } finally {
            statement.close();
        }
    }
    
    /**
     * 管理テーブルの削除
     * 
     * @throws SQLException 例外
     */
    protected static void dropManageTable() throws SQLException {
        Statement statement = con.createStatement();
        try {
            statement.execute("DROP TABLE CAPTCHA_MANAGE PURGE");
        } catch (Exception e) {
            // nop
        } finally {
            statement.close();
        }
    }
    
    /**
     * メッセージテーブルの作成
     * 
     * @throws SQLException 例外
     */
    protected static void createMessageTable() throws SQLException {
        Statement statement = con.createStatement();
        try {
            statement.execute("CREATE TABLE MESSAGE (MESSAGE_ID VARCHAR2(10) PRIMARY KEY, LANG CHAR(2), MESSAGE NVARCHAR2(200))");
        } catch (Exception e) {
            // nop
        } finally {
            statement.close();
        }
    }
    
    /**
     * メッセージテーブルの削除
     * 
     * @throws SQLException 例外
     */
    protected static void dropMessageTable() throws SQLException {
        Statement statement = con.createStatement();
        try {
            statement.execute("DROP TABLE MESSAGE PURGE");
        } catch (Exception e) {
            // nop
        } finally {
            statement.close();
        }
    }
    
    /**
     * テスト対象が使用するトランザクションのコミット
     */
    protected static void commitBizTran() {
        tmConn.commit();
    }
    
    /**
     * テスト対象が使用するトランザクションのロールバック
     */
    protected static void rollbackBizTran() {
        tmConn.rollback();
    }
    
    /**
     * テストデータのセットアップや検証を行うトランザクションのコミット
     */
    protected static void commitTestTran() {
        try {
            con.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * テストデータのセットアップや検証を行うトランザクションのロールバック
     */
    protected static void rollbackTestTran() {
        try {
            con.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * テストデータのセットアップや検証を行うコネクションの取得
     * @return コネクション
     */
    protected static Connection getTestConnection() {
        return con;
    }
    
    /**
     * キー指定でCAPTCHA情報の件数を取得する。
     * 
     * @param key 識別キー
     * @return CAPTCHA情報の件数
     * @throws SQLException 例外
     */
    protected int countManageTableByKey(String key) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement("SELECT COUNT(*) FROM CAPTCHA_MANAGE WHERE CAPTCHA_KEY = ?");
        ResultSet rs = null;
        int count = 0;
        try {
            pstmt.setString(1, key);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            pstmt.close();
        }
        return count;
    }
    
    /**
     * キー指定でCAPTCHA情報を取得する。
     * 
     * @param key 識別キー
     * @return CAPTCHA情報
     * @throws SQLException 例外
     */
    protected Captcha getFromManageTableByKey(String key) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement("SELECT CAPTCHA_KEY, CAPTCHA_TEXT, GENERATE_DATE_TIME FROM CAPTCHA_MANAGE WHERE CAPTCHA_KEY = ?");
        ResultSet rs = null;
        Captcha captcha = new Captcha();
        try {
            pstmt.setString(1, key);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                captcha.setKey(rs.getString(1));
                captcha.setText(rs.getString(2));
                captcha.setGenerateDateTime(rs.getTimestamp(3));
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            pstmt.close();
        }
        return captcha;
    }
    
    /**
     * CAPTCHA情報を登録する。
     * 
     * @param captcha CAPTCHA情報
     * @throws SQLException 例外
     * @return 件数
     */
    protected int insertIntoManageTableByKey(Captcha captcha) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement("INSERT INTO CAPTCHA_MANAGE (CAPTCHA_KEY, CAPTCHA_TEXT, GENERATE_DATE_TIME) VALUES (?, ?, ?)");
        try {
            pstmt.setString(1, captcha.getKey());
            pstmt.setString(2, captcha.getText());
            pstmt.setTimestamp(3, captcha.getGenerateDateTime() != null ? new Timestamp(captcha.getGenerateDateTime().getTime()) : null);
            
            return pstmt.executeUpdate();
            
        } finally {
            pstmt.close();
        }
    }
    
    /**
     * CAPTCHA情報を削除する。
     * 
     * @param key 識別キー
     * @throws SQLException 例外
     * @return 件数
     */
    protected int deleteFromManageTableByKey(String key) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement("DELETE FROM CAPTCHA_MANAGE WHERE CAPTCHA_KEY = ?");
        try {
            pstmt.setString(1, key);
            
            return pstmt.executeUpdate();
            
        } finally {
            pstmt.close();
        }
    }
    
    /**
     * MESSAGEを登録する。
     */
    protected static int insertIntoMessageTable(String messageId, String lang, String message) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement("INSERT INTO MESSAGE (MESSAGE_ID, LANG, MESSAGE) VALUES (?, ?, ?)");
        try {
            pstmt.setString(1, messageId);
            pstmt.setString(2, lang);
            pstmt.setString(3, message);
            return pstmt.executeUpdate();
            
        } finally {
            pstmt.close();
        }
    }
    
}

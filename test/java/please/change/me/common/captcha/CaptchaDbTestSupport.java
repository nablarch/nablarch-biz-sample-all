package please.change.me.common.captcha;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import please.change.me.common.captcha.entity.CaptchaManage;
import please.change.me.common.captcha.entity.CaptchaMessage;

import nablarch.core.db.connection.ConnectionFactory;
import nablarch.core.db.connection.TransactionManagerConnection;
import nablarch.core.repository.SystemRepository;
import nablarch.test.support.db.helper.VariousDbTestHelper;

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
     * テスト対象が使用するコネクションを取得する。
     * @return
     */
    public static TransactionManagerConnection getTmConn() {
        return tmConn;
    }

    /**
     * DB関係セットアップ。
     *
     * テスト時に使用するデータベース接続の生成及びテスト用のテーブルのセットアップを行う。
     *
     * @throws SQLException 例外
     */
    protected static void setupDb() throws SQLException {
        VariousDbTestHelper.createTable(CaptchaManage.class);
        VariousDbTestHelper.createTable(CaptchaMessage.class);

        VariousDbTestHelper.insert(new CaptchaMessage("MSG90001", "ja", "{0}が正しくありません。"));

        ConnectionFactory factory = SystemRepository.get("connectionFactory");
        tmConn = factory.getConnection("test");
        con = VariousDbTestHelper.getNativeConnection();
        con.setAutoCommit(false);
    }
    
    /**
     * DB関係終了処理。
     *
     */
    protected static void teardownDb() {
        VariousDbTestHelper.dropTable(CaptchaManage.class);
        VariousDbTestHelper.dropTable(CaptchaMessage.class);
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
     * キー指定でCAPTCHA情報の件数を取得する。
     * 
     * @param key 識別キー
     * @return CAPTCHA情報の件数
     */
    protected int countManageTableByKey(String key) {
        return (VariousDbTestHelper.findById(CaptchaManage.class, key) != null) ? 1 : 0;
    }
    
    /**
     * キー指定でCAPTCHA情報を取得する。
     * 
     * @param key 識別キー
     * @return CAPTCHA情報
     */
    protected Captcha getFromManageTableByKey(String key) {
        CaptchaManage manage = VariousDbTestHelper.findById(CaptchaManage.class, key);
        Captcha captcha = new Captcha();
        if (manage != null) {
            captcha.setKey(manage.captchaKey);
            captcha.setText(manage.captchaText);
            captcha.setGenerateDateTime(manage.generateDateTime);
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
}

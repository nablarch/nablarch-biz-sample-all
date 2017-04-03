package please.change.me.common.captcha;

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
     */
    protected static void setupDb() {
        VariousDbTestHelper.createTable(CaptchaManage.class);
        VariousDbTestHelper.createTable(CaptchaMessage.class);

        VariousDbTestHelper.insert(new CaptchaMessage("MSG90001", "ja", "{0}が正しくありません。"));

        ConnectionFactory factory = SystemRepository.get("connectionFactory");
        tmConn = factory.getConnection("test");
    }
    
    /**
     * DB関係終了処理。
     *
     */
    protected static void teardownDb() {
        if (tmConn != null) {
            tmConn.terminate();
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
     * @return 件数
     */
    protected void insertIntoManageTableByKey(Captcha captcha) {
        Timestamp timestamp = (captcha.getGenerateDateTime() != null) ? new Timestamp(captcha.getGenerateDateTime().getTime()) : null;
        VariousDbTestHelper.insert(new CaptchaManage(
                captcha.getKey(),
                captcha.getText(),
                timestamp));
    }
    
    /**
     * CAPTCHA情報を削除する。
     * 
     */
    protected void deleteFromManageTable() {
        VariousDbTestHelper.delete(CaptchaManage.class);
    }
}

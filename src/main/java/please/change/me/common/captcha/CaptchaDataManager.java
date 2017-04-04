package please.change.me.common.captcha;

import java.sql.Timestamp;

import nablarch.core.date.SystemTimeUtil;
import nablarch.core.db.statement.SqlPStatement;
import nablarch.core.db.statement.SqlResultSet;
import nablarch.core.db.statement.SqlRow;
import nablarch.core.db.support.DbAccessSupport;
import nablarch.core.util.StringUtil;

/**
 * CAPTCHAデータマネージャー
 * 
 * @author TIS
 */
public class CaptchaDataManager extends DbAccessSupport {

    /**
     * DBにキャプチャ管理情報を作成します。
     * 
     * @param key 識別キー
     */
    public void create(String key) {
        SqlPStatement stmt = getSqlPStatement("INSERT_CAPTCHA_MANAGE");
        stmt.setString(1, key);
        stmt.setTimestamp(2, SystemTimeUtil.getTimestamp());
        stmt.executeUpdate();
    }
    
    /**
     * DBにキャプチャ情報を保存します。
     * 
     * @param captcha キャプチャ情報
     * @return 画像の保存結果
     */
    public boolean save(Captcha captcha) {
        if (captcha ==  null) {
            throw new IllegalArgumentException("captcha is null.");
        }
        if (StringUtil.isNullOrEmpty(captcha.getKey())) {
            throw new IllegalArgumentException("captcha.key is null.");
        }
        if (StringUtil.isNullOrEmpty(captcha.getText())) {
            throw new IllegalArgumentException("captcha.text is null.");
        }
        if (captcha.getGenerateDateTime() ==  null) {
            throw new IllegalArgumentException("captcha.generateDateTime is null.");
        }
        
        SqlPStatement stmt = getSqlPStatement("UPDATE_CAPTCHA_MANAGE");
        stmt.setString(1, captcha.getText());
        stmt.setTimestamp(2, new Timestamp(captcha.getGenerateDateTime().getTime()));
        stmt.setString(3, captcha.getKey());
        
        int count = stmt.executeUpdate();
        
        return count == 1;
    }
    
    /**
     * DBよりキャプチャ情報を取得します。
     * @param key 識別キー
     * @return 識別キーに該当するキャプチャ情報。取得できない場合はnull
     */
    public Captcha load(String key) {
        if (StringUtil.isNullOrEmpty(key)) {
            throw new IllegalArgumentException("key is null.");
        }
        
        SqlPStatement stmt = getSqlPStatement("SELECT_CAPTCHA_MANAGE");
        stmt.setString(1, key);
        SqlResultSet rs = stmt.retrieve();
        
        Captcha captcha = null;
        
        if (!rs.isEmpty()) {
            SqlRow row = rs.get(0);
            
            captcha = new Captcha()
                        .setKey(row.getString("CAPTCHA_KEY"))
                        .setText(row.getString("CAPTCHA_TEXT"))
                        .setGenerateDateTime(row.getTimestamp("GENERATE_DATE_TIME"));
        }
        
        return captcha;
    }
}

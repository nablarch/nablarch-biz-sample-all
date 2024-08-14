package please.change.me.form;

import java.io.Serializable;

/**
 * 応答電文に設定する項目を保持するためのForm。
 * @author Nabu Rakutaro
 */
public class ProjectInsertMessageResponseForm implements Serializable {
    /**
     * serialVersionUID。
     */
    private static final long serialVersionUID = 1L;

    /**
     * リターンコード。
     */
    private final String returnCode;

    /**
     * 詳細情報。
     */
    private final String detail;

    /**
     * コンストラクタ。
     * @param returnCode リターンコード
     * @param detail 詳細情報
     */
    public ProjectInsertMessageResponseForm(String returnCode, String detail) {
        this.returnCode = returnCode;
        this.detail = detail;
    }

    /**
     * リターンコードを取得する。
     * @return リターンコード。
     */
    public String getReturnCode() {
        return returnCode;
    }


    /**
     * 詳細情報を取得する。
     * @return 詳細情報。
     */
    public String getDetail() {
        return detail;
    }
}

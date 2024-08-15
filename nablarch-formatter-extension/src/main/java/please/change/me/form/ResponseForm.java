package please.change.me.form;

import java.io.Serializable;

/**
 * 応答電文に設定する項目を保持するためのForm。
 */
public class ResponseForm implements Serializable {

    /** シリアルバージョンUID */
    private static final long serialVersionUID = 1L;

    /** 項目1 */
    private String key1;

    /** 項目2 */
    private String key2;

    /**
     * 項目1を返します。
     *
     * @return 項目1
     */
    public String getKey1() {
        return key1;
    }

    /**
     * 項目1を設定します。
     *
     * @param key1 項目1
     */
    public void setKey1(String key1) {
        this.key1 = key1;
    }

    /**
     * 項目2を返します。
     *
     * @return 項目2
     */
    public String getKey2() {
        return key2;
    }

    /**
     * 項目2を設定します。
     *
     * @param key2 項目2
     */
    public void setKey2(String key2) {
        this.key2 = key2;
    }
}

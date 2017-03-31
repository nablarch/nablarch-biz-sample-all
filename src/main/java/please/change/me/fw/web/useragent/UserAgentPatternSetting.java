package please.change.me.fw.web.useragent;

import nablarch.fw.web.useragent.UserAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * UserAgent判定・パターン定義設定クラス。
 *
 * @author TIS
 */
public class UserAgentPatternSetting {

    /** 項目タイプデフォルト値 */
    private String defaultType = UserAgent.DEFAULT_TYPE_VALUE;

    /** 項目名称デフォルト値 */
    private String defaultName = UserAgent.DEFAULT_NAME_VALUE;

    /** バージョンデフォルト値 */
    private String defaultVersion = UserAgent.DEFAULT_VERSION_VALUE;

    /** 項目タイプパターン定義リスト */
    private List<TypePattern> typePatternList = new ArrayList<TypePattern>();

    /** 名称、バージョンパターン定義リスト */
    private List<ItemPattern> itemPatternList = new ArrayList<ItemPattern>();

    /**
     * 項目タイプパターン定義リストを取得する。
     *
     * @return 項目タイプパターン定義リスト
     */
    public List<TypePattern> getTypePatternList() {
        return typePatternList;
    }

    /**
     * 項目タイプパターン定義リストをセットする。
     *
     * @param typePatternList 項目タイプパターン定義リスト
     */
    public void setTypePatternList(List<TypePattern> typePatternList) {
        if (typePatternList == null) {
            throw new IllegalArgumentException("typeListPatternList must not be null.");
        }
        this.typePatternList = typePatternList;
    }

    /**
     * 項目タイプパターン定義を追加する。
     *
     * @param typePattern 項目タイプパターン定義
     */
    public void addTypePattern(TypePattern typePattern) {
        typePatternList.add(typePattern);
    }

    /**
     * 名称、バージョンパターン定義リストを取得する。
     *
     * @return 名称、バージョンパターン定義リスト
     */
    public List<ItemPattern> getItemPatternList() {
        return itemPatternList;
    }

    /**
     * 名称、バージョンパターン定義リストをセットする。
     *
     * @param itemPatternList 名称、バージョンパターン定義リスト
     */
    public void setItemPatternList(List<ItemPattern> itemPatternList) {
        if (itemPatternList == null) {
            throw new IllegalArgumentException("itemPatternList must not be null.");
        }
        this.itemPatternList = itemPatternList;
    }

    /**
     * 名称、バージョンパターン定義を追加する。
     *
     * @param itemPattern 名称、バージョンパターン定義
     */
    public void addItemPattern(ItemPattern itemPattern) {
        itemPatternList.add(itemPattern);
    }

    /**
     * デフォルト項目タイプを取得する。
     *
     * @return デフォルト項目タイプ
     */
    public String getDefaultType() {
        return defaultType;
    }

    /**
     * デフォルト項目タイプをセットする。
     *
     * @param defaultType デフォルト項目タイプ

     */
    public void setDefaultType(String defaultType) {
        this.defaultType = defaultType;
    }

    /**
     * デフォルト項目名称を取得する。
     *
     * @return デフォルト項目名称
     */
    public String getDefaultName() {
        return defaultName;
    }

    /**
     * デフォルト項目名称をセットする。
     *
     * @param defaultName デフォルト項目名称
     */
    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    /**
     * デフォルトバージョンを取得する。
     *
     * @return デフォルトバージョン
     */
    public String getDefaultVersion() {
        return defaultVersion;
    }

    /**
     * デフォルトバージョンをセットする。
     *
     * @param defaultVersion デフォルトバージョン
     */
    public void setDefaultVersion(String defaultVersion) {
        this.defaultVersion = defaultVersion;

    }
}

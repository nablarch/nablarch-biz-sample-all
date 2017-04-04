package please.change.me.fw.web.useragent;

/**
 * 項目タイプパターンクラス。
 *
 * @author T.Kawasaki
 */
public class TypePattern {

    /** 抽出パターン */
    private ExtractionPattern pattern = new ExtractionPattern();

    /** 項目名 */
    private String typeName;


    /**
     * 項目名を取得する。
     * @param uaText User-Agent文字列
     * @return 項目名
     */
    String getName(String uaText) {
        if (pattern.getMatcherOf(uaText).matches()) {
            return typeName;
        }
        return null;
    }

    /**
     * 項目名を設定する。
     * @param typeName 項目名
     */
    public void setName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * 抽出パターンを設定する。
     * @param pattern 抽出パターン
     */
    public void setPattern(String pattern) {
        this.pattern.setPattern(pattern);
    }
}

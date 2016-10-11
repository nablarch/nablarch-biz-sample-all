package please.change.me.fw.web.useragent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 抽出パターンクラス。
 * 項目名称パターン、項目タイプパターンを保持する用途に使用する。
 *
 * @author T.Kawasaki
 * @see ItemPattern
 * @see TypePattern
 */
class ExtractionPattern {

    /** 抽出パターン */
    private Pattern pattern;

    /**
     * 設定された抽出パターンを元に、指定された文字列に対するMatcherを取得する。
     * @param uaText 文字列
     * @return Matcher
     */
    Matcher getMatcherOf(String uaText) {
        return getPattern().matcher(uaText);
    }

    /**
     * 抽出パターンを取得する。
     * @return 抽出パターン
     */
    Pattern getPattern() {
        if (!isAlreadyPatternSet()) {
            throw new IllegalStateException("pattern must be set.");
        }
        return pattern;
    }

    /**
     * 抽出パターンが設定済みかどうか判定する。
     * @return 設定済みの場合、真
     */
    boolean isAlreadyPatternSet() {
        return pattern != null;
    }

    /**
     * 抽出パターンを設定する。
     * @param pattern 抽出パターン
     */
    void setPattern(String pattern) {
        try {
            this.pattern = Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException(
                    "invalid pattern was specified. pattern=[" + pattern + "]", e);
        }
    }

    /**
     * 抽出パターン文字列を取得する。
     * @return 抽出パターンの文字列表現
     */
    @Override
    public String toString() {
        return String.valueOf(pattern);
    }
}

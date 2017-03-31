package please.change.me.fw.web.useragent;

import java.util.regex.Matcher;

/**
 * 項目名称パターンクラス。
 *
 * @author T.Kawasaki
 */
public class ItemPattern {

    /** 抽出パターン */
    private ExtractionPattern pattern = new ExtractionPattern();

    /** 項目名称抽出グループ番号 */
    private Integer nameIndex;

    /** バージョン抽出グループ番号 */
    private Integer versionIndex;

    /** 項目名称コンバータ */
    private UserAgentValueConvertor nameConvertor = new NopUserAgentConvertor();

    /** バージョンコンバータ */
    private UserAgentValueConvertor versionConvertor = new NopUserAgentConvertor();

    /** 項目名称({@link #nameIndex}が設定されていない場合に使用する） */
    private String name;

    /**
     * 項目名称抽出グループ番号を設定する。
     *
     * @param nameIndex 項目名称抽出グループ番号
     */
    public void setNameIndex(Integer nameIndex) {
        checkNameIndexRange(nameIndex);
        this.nameIndex = nameIndex;

    }

    /**
     * バージョン抽出グループ番号を設定する。
     *
     * @param versionIndex バージョン抽出グループ番号
     */
    public void setVersionIndex(Integer versionIndex) {
        checkVersionIndexRange(versionIndex);
        this.versionIndex = versionIndex;
    }

    /**
     * バージョンを取得する。
     * {@link #setVersionIndex(Integer)}でインデックスが設定されていない場合、
     * または、パターンに一致しない場合、nullを返却する。
     *
     * @param uaText User-Agent文字列
     * @return バージョン
     */
    public String getVersion(String uaText) {
        if (isVersionIndexSpecified()) {
            return doGetVersion(uaText);
        }
        return null;
    }

    /**
     * バージョンを取得する。
     * パターンに一致しない場合、nullを返却する。
     *
     * @param uaText User-Agent文字列
     * @return バージョン
     */
    private String doGetVersion(String uaText) {
        Matcher matcher = pattern.getMatcherOf(uaText);
        if (!matcher.matches()) {
            return null;
        }
        String version = matcher.group(versionIndex);
        return versionConvertor.convert(version);
    }

    /**
     * バージョンのインデックス指定がされているかどうか判定する。
     *
     * @return 指定されている場合、真
     */
    private boolean isVersionIndexSpecified() {
        return versionIndex != null;
    }

    /**
     * User-Agent文字列から、パターンにマッチした名称を取得する。
     *
     * @param uaText User-Agent文字列
     * @return 名称（パターンにマッチしない場合はnull）
     */
    String getName(String uaText) {
        Matcher matcher = pattern.getMatcherOf(uaText);

        if (!matcher.matches()) {
            return null;
        }

        String name = nameIndex != null ? matcher.group(nameIndex) : this.name;
        return nameConvertor.convert(name);

    }

    /**
     * 名称のコンバータを設定する。
     *
     * @param nameConvertor コンバータ(null不可)
     */
    public void setNameConvertor(UserAgentValueConvertor nameConvertor) {
        if (nameConvertor == null) {
            throw new IllegalArgumentException("nameConverter must not be null.");
        }

        this.nameConvertor = nameConvertor;
    }

    /**
     * バージョンのコンバータを設定する。
     *
     * @param versionConvertor コンバータ(null不可)
     */
    public void setVersionConvertor(UserAgentValueConvertor versionConvertor) {
        if (versionConvertor == null) {
            throw new IllegalArgumentException("nameConverter must not be null.");
        }
        this.versionConvertor = versionConvertor;
    }

    /**
     * 項目名称を設定する。
     * 名称インデックス（{@link #setNameIndex(Integer)}が指定されていない場合、
     * この項目名称が使用される。
     *
     * @param name 項目名称
     */
    public void setName(String name) {
        this.name = name;
    }


    /** {@inheritDoc} */
    public void setPattern(String pattern) {
        this.pattern.setPattern(pattern);
        checkIndexRange();    // 実行前に設定エラーを発見するため、設定時に可能な限りのチェックを行う。
    }

    /**
     * インデックスの範囲チェックを行う。
     */
    private void checkIndexRange() {
        checkNameIndexRange(nameIndex);
        checkVersionIndexRange(versionIndex);
    }


    /**
     * 項目名称インデックスの範囲チェックを行う。
     *
     * @param nameIndex 項目名称インデックス
     */
    private void checkNameIndexRange(Integer nameIndex) {
        if (nameIndex == null) {
            return;
        }
        if (nameIndex <= 0) {
            throw new IllegalArgumentException(
                    "nameIndex must not be zero or negative.but was [" + nameIndex + "].");
        }
        if (!pattern.isAlreadyPatternSet()) {
            return; // パターン未設定の場合は以下のチェックをスキップ（パターン設定時に実施）
        }

        Matcher m = getMatcherOf(""); // ダミー文字列でMatcherを取得し、グループカウントを取得する
        if (nameIndex > m.groupCount()) {
            throw new IllegalArgumentException(
                    "invalid name index was specified. "
                            + "index=[" + nameIndex + "] pattern=[" + pattern + "]");
        }
    }

    /**
     * バージョンインデックスの範囲チェックを行う。
     *
     * @param versionIndex バージョンインデックス
     */
    private void checkVersionIndexRange(Integer versionIndex) {
        if (versionIndex == null) {
            return;
        }
        if (versionIndex <= 0) {
            throw new IllegalArgumentException(
                    "versionIndex must not be zero or negative.but was [" + versionIndex + "]");

        }
        if (!pattern.isAlreadyPatternSet()) {
            return; // パターン未設定の場合はチェックしない。
        }


        Matcher m = getMatcherOf(""); // ダミー文字列でMatcherを取得し、グループカウントを取得する
        if (versionIndex > m.groupCount()) {
            throw new IllegalArgumentException(
                    "invalid version index was specified. "
                            + "index=[" + versionIndex + "] pattern=[" + pattern + "]");
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "{"
                + "pattern= " + pattern
                + ", nameIndex=" + nameIndex
                + ", versionIndex=" + versionIndex
                + ", nameConvertor=" + nameConvertor
                + ", versionConvertor=" + versionConvertor
                + ", name='" + name + '\''
                + "}";
    }

    /**
     *
     * @param uaText UserAgent文字列
     * @return Matcher
     */
    Matcher getMatcherOf(String uaText) {
        return pattern.getMatcherOf(uaText);
    }

    /**
     * {@link UserAgentValueConvertor}のデフォルト値に使用する
     */
    private static class NopUserAgentConvertor implements UserAgentValueConvertor {
        @Override
        public String convert(String value) {
            return value;
        }
    }
}

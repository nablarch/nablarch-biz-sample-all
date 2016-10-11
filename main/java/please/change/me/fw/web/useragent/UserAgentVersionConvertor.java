package please.change.me.fw.web.useragent;

import nablarch.core.util.StringUtil;

/**
 * UserAgent判定・項目名称変換デフォルト実装クラス。
 * <p/>
 * <p>
 * 文字列を「\D(数字以外)」で分割し、３つの要素にする。
 * それぞれの要素をパディングし、スペース区切りで結合した値を変換値とする。
 * <br>
 * 例）「34.0.1847.116」→「_34 __0 ___1847」
 * </p>
 *
 * @author TIS
 */
public class UserAgentVersionConvertor implements UserAgentValueConvertor {

    /** パディング文字 */
    private String padding;

    /**
     * {@inheritDoc}
     */
    public String convert(String value) {
        if (value == null) {
            return null;
        }

        if (padding == null) {
            return value;
        }

        String[] exprList = {
                padding, " " + StringUtil.repeat(padding, 2),
                " " + StringUtil.repeat(padding, 3)
        };
        String[] vers = value.split("\\D");
        for (int i = 0; i < vers.length && i < exprList.length; i++) {
            exprList[i] = exprList[i] + vers[i];
        }
        StringBuilder exp = new StringBuilder();
        for (String v : exprList) {
            exp.append(v);
        }
        return exp.toString();

    }

    /**
     * パディング文字を設定する。
     *
     * @param padding パディング文字
     */
    public void setPadding(String padding) {
        this.padding = padding;
    }
}

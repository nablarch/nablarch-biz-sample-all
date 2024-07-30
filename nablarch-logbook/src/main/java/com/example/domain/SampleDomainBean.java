package com.example.domain;

import nablarch.core.validation.ee.Length;
import nablarch.core.validation.ee.SystemChar;

/**
 * ドメイン定義の例。
 */
public class SampleDomainBean {

    /** ユーザ氏名（漢字）。 */
    @Length(max = 50)
    @SystemChar(charsetDef = "全角文字", allowLineSeparator = false)
    public String userKanjiName;

    /** ユーザ氏名（ひらがな）。 */
    @Length(max = 50)
    @SystemChar(charsetDef = "全角ひらがな", allowLineSeparator = false)
    public String userKanaName;

}

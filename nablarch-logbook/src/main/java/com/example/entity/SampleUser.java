package com.example.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 疎通確認用のEntityクラス。
 * <p>
 * 補足：<br>
 * 本クラスはH2 Database Engine用です。
 * </p>
 *
 * @deprecated TODO 疎通確認用のクラスです。確認完了後、削除してください。
 */
@Entity
@Table(schema = "PUBLIC", name = "SAMPLE_USER")
public class SampleUser implements Serializable {

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** ユーザ情報ID。 */
    private Integer userId;

    /** 漢字氏名。 */
    private String kanjiName;

    /** かな氏名。 */
    private String kanaName;
    /**
     * ユーザ情報IDを返します。
     *
     * @return ユーザ情報ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID", precision = 10, nullable = false, unique = true)
    public Integer getUserId() {
        return userId;
    }

    /**
     * ユーザ情報IDを設定します。
     *
     * @param userId ユーザ情報ID
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    /**
     * 漢字氏名を返します。
     *
     * @return 漢字氏名
     */
    @Column(name = "KANJI_NAME", length = 50, nullable = false, unique = false)
    public String getKanjiName() {
        return kanjiName;
    }

    /**
     * 漢字氏名を設定します。
     *
     * @param kanjiName 漢字氏名
     */
    public void setKanjiName(String kanjiName) {
        this.kanjiName = kanjiName;
    }
    /**
     * かな氏名を返します。
     *
     * @return かな氏名
     */
    @Column(name = "KANA_NAME", length = 50, nullable = false, unique = false)
    public String getKanaName() {
        return kanaName;
    }

    /**
     * かな氏名を設定します。
     *
     * @param kanaName かな氏名
     */
    public void setKanaName(String kanaName) {
        this.kanaName = kanaName;
    }
}

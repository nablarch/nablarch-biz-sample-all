package com.example.dto;

import java.io.Serializable;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.example.entity.SampleUser;

/**
 * 疎通確認用の検索結果格納用Dtoクラス。
 *
 * @deprecated TODO 疎通確認用のクラスです。確認完了後、削除してください。
 */
@XmlRootElement(name = "userList")
@XmlAccessorType(XmlAccessType.FIELD)
public class SampleUserListDto implements Serializable {

    @XmlElement(name = "sampleUser")
    private List<SampleUser> sampleUserList;

    /**
     * デフォルトコンストラクタ。
     */
    public SampleUserListDto() {
    }

    /**
     * コンストラクタ。
     * @param sampleUserList ユーザのリスト
     */
    public SampleUserListDto(List<SampleUser> sampleUserList) {
        super();
        this.sampleUserList = sampleUserList;
    }

    /**
     * ユーザのリストを取得します。
     * @return ユーザ情報のリスト
     */
    public List<SampleUser> getSampleUserList() {
        return sampleUserList;
    }

    /**
     * ユーザのリストを設定します。
     * @param sampleUserList ユーザ情報のリスト
     */
    public void setSampleUserList(List<SampleUser> sampleUserList) {
        this.sampleUserList = sampleUserList;
    }

}

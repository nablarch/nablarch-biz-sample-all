package please.change.me.form;

import java.io.Serializable;

import nablarch.core.validation.ee.Domain;
import nablarch.core.validation.ee.Required;

/**
 * 受信電文に対するバリデーションのために使用するForm。
 * @author Nabu Rakutaro
 */
public class ProjectInsertMessageForm implements Serializable {

    /** シリアルバージョンUID */
    private static final long serialVersionUID = 1L;

    /** プロジェクト名 */
    @Domain("projectName")
    @Required
    private String projectName;

    /** プロジェクト種別 */
    @Domain("projectType")
    @Required
    private String projectType;

    /** プロジェクト分類 */
    @Domain("projectClass")
    @Required
    private String projectClass;

    /** プロジェクト開始日付 */
    @Domain("date")
    private String projectStartDate;

    /** プロジェクト終了日付 */
    @Domain("date")
    private String projectEndDate;

    /** 顧客ID */
    @Domain("clientId")
    private String clientId;

    /** プロジェクトマネージャー */
    @Domain("projectManager")
    private String projectManager;

    /** プロジェクトリーダー */
    @Domain("projectLeader")
    private String projectLeader;

    /** ユーザID */
    @Domain("userId")
    @Required
    private String userId;

    /** 備考 */
    @Domain("note")
    private String note;

    /** 売上高 */
    @Domain("sales")
    private String sales;

    /** 売上原価 */
    @Domain("costOfGoodsSold")
    private String costOfGoodsSold;

    /** 販管費 */
    @Domain("sga")
    private String sga;

    /** 本社配賦 */
    @Domain("allocationOfCorpExpenses")
    private String allocationOfCorpExpenses;

    /**
     * プロジェクト名を返します。
     *
     * @return プロジェクト名
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * プロジェクト名を設定します。
     *
     * @param projectName プロジェクト名
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    /**
     * プロジェクト種別を返します。
     *
     * @return プロジェクト種別
     */
    public String getProjectType() {
        return projectType;
    }

    /**
     * プロジェクト種別を設定します。
     *
     * @param projectType プロジェクト種別
     */
    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }
    /**
     * プロジェクト分類を返します。
     *
     * @return プロジェクト分類
     */
    public String getProjectClass() {
        return projectClass;
    }

    /**
     * プロジェクト分類を設定します。
     *
     * @param projectClass プロジェクト分類
     */
    public void setProjectClass(String projectClass) {
        this.projectClass = projectClass;
    }
    /**
     * プロジェクト開始日付を返します。
     *
     * @return プロジェクト開始日付
     */
    public String getProjectStartDate() {
        return projectStartDate;
    }

    /**
     * プロジェクト開始日付を設定します。
     *
     * @param projectStartDate プロジェクト開始日付
     */
    public void setProjectStartDate(String projectStartDate) {
        this.projectStartDate = projectStartDate;
    }
    /**
     * プロジェクト終了日付を返します。
     *
     * @return プロジェクト終了日付
     */
    public String getProjectEndDate() {
        return projectEndDate;
    }

    /**
     * プロジェクト終了日付を設定します。
     *
     * @param projectEndDate プロジェクト終了日付
     */
    public void setProjectEndDate(String projectEndDate) {
        this.projectEndDate = projectEndDate;
    }
    /**
     * 顧客IDを返します。
     *
     * @return 顧客ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * 顧客IDを設定します。
     *
     * @param clientId 顧客ID
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    /**
     * プロジェクトマネージャーを返します。
     *
     * @return プロジェクトマネージャー
     */
    public String getProjectManager() {
        return projectManager;
    }

    /**
     * プロジェクトマネージャーを設定します。
     *
     * @param projectManager プロジェクトマネージャー
     */
    public void setProjectManager(String projectManager) {
        this.projectManager = projectManager;
    }
    /**
     * プロジェクトリーダーを返します。
     *
     * @return プロジェクトリーダー
     */
    public String getProjectLeader() {
        return projectLeader;
    }

    /**
     * プロジェクトリーダーを設定します。
     *
     * @param projectLeader プロジェクトリーダー
     */
    public void setProjectLeader(String projectLeader) {
        this.projectLeader = projectLeader;
    }
    /**
     * ユーザIDを返します。
     *
     * @return ユーザID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * ユーザIDを設定します。
     *
     * @param userId ユーザID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    /**
     * 備考を返します。
     *
     * @return 備考
     */
    public String getNote() {
        return note;
    }

    /**
     * 備考を設定します。
     *
     * @param note 備考
     */
    public void setNote(String note) {
        this.note = note;
    }
    /**
     * 売上高を返します。
     *
     * @return 売上高
     */
    public String getSales() {
        return sales;
    }

    /**
     * 売上高を設定します。
     *
     * @param sales 売上高
     */
    public void setSales(String sales) {
        this.sales = sales;
    }
    /**
     * 売上原価を返します。
     *
     * @return 売上原価
     */
    public String getCostOfGoodsSold() {
        return costOfGoodsSold;
    }

    /**
     * 売上原価を設定します。
     *
     * @param costOfGoodsSold 売上原価
     */
    public void setCostOfGoodsSold(String costOfGoodsSold) {
        this.costOfGoodsSold = costOfGoodsSold;
    }
    /**
     * 販管費を返します。
     *
     * @return 販管費
     */
    public String getSga() {
        return sga;
    }

    /**
     * 販管費を設定します。
     *
     * @param sga 販管費
     */
    public void setSga(String sga) {
        this.sga = sga;
    }
    /**
     * 本社配賦を返します。
     *
     * @return 本社配賦
     */
    public String getAllocationOfCorpExpenses() {
        return allocationOfCorpExpenses;
    }

    /**
     * 本社配賦を設定します。
     *
     * @param allocationOfCorpExpenses 本社配賦
     */
    public void setAllocationOfCorpExpenses(String allocationOfCorpExpenses) {
        this.allocationOfCorpExpenses = allocationOfCorpExpenses;
    }
}

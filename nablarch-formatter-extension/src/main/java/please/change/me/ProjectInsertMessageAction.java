package please.change.me;

import java.sql.Timestamp;

import please.change.me.entity.ProjectTemp;
import please.change.me.form.ProjectInsertMessageForm;
import please.change.me.form.ProjectInsertMessageResponseForm;

import nablarch.common.dao.UniversalDao;
import nablarch.core.beans.BeanUtil;
import nablarch.core.dataformat.InvalidDataFormatException;
import nablarch.core.date.SystemTimeUtil;
import nablarch.core.message.ApplicationException;
import nablarch.core.validation.ee.ValidatorUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.messaging.RequestMessage;
import nablarch.fw.messaging.ResponseMessage;
import nablarch.fw.messaging.action.MessagingAction;

/**
 * プロジェクト登録要求電文を受信して、一時テーブルに電文の情報を格納する。
 * (MOM同期応答メッセージング実行制御基盤を使用)
 * @author Nabu Rakutaro
 */
public class ProjectInsertMessageAction extends MessagingAction {

    // ------------ 正常系制御 --------------- //
    /**
     * データ部に格納された登録ユーザレコードの項目バリデーションを行った後、
     * データベースに登録する。
     *
     * バリデーションエラーとなった場合は、処理をロールバックしてエラー応答を送信する。
     *
     * @param request 要求電文オブジェクト
     * @param context 実行コンテキスト
     * @return 応答電文オブジェクト
     */
    @Override
    protected ResponseMessage onReceive(RequestMessage request,
            ExecutionContext context) {
        ProjectInsertMessageForm form = BeanUtil.createAndCopy(ProjectInsertMessageForm.class, request.getParamMap());

        //バリデーション処理を行う。エラー検知時は、ApplicationExceptionが送出される。
        ValidatorUtil.validate(form);

        ProjectTemp projectTemp = BeanUtil.createAndCopy(ProjectTemp.class, form);

        //対応するプロパティがコピー元に存在しないものについて手動で設定。
        projectTemp.setStatus("0");
        Timestamp now = SystemTimeUtil.getTimestamp();
        projectTemp.setInsertDate(now);
        projectTemp.setUpdateDate(now);

        // データをDBに登録する
        UniversalDao.insert(projectTemp);

        // 応答データ返却
        ProjectInsertMessageResponseForm resForm = new ProjectInsertMessageResponseForm("success", "");
        return request.reply().addRecord(resForm);
    }


    // ------------ 異常系制御 --------------- //
    /**
     * 業務処理がエラー終了した場合に送信する応答電文の内容を設定する。
     *
     * @param e       発生した例外オブジェクト
     * @param request 要求電文オブジェクト
     * @param context 実行コンテキスト
     * @return 応答電文オブジェクト
     */
    @Override
    protected ResponseMessage onError(Throwable e, RequestMessage request, ExecutionContext context) {
        final ProjectInsertMessageResponseForm resForm;

        if (e instanceof InvalidDataFormatException) {
            //要求電文データレコード部レイアウト不正
            resForm = new ProjectInsertMessageResponseForm("fatal", "invalid layout.");
        } else if (e instanceof ApplicationException) {
            //要求電文データレコード部項目バリデーションエラー
            resForm = new ProjectInsertMessageResponseForm("error.validation", "");
        } else {
            resForm = new ProjectInsertMessageResponseForm("fatal", "unexpected exception.");
        }
        return request.reply().addRecord(resForm);
    }
}

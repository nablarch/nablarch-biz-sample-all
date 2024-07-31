package please.change.me;

import nablarch.common.web.download.StreamResponse;
import nablarch.common.web.token.OnDoubleSubmission;
import nablarch.core.date.SystemTimeUtil;
import nablarch.core.message.ApplicationException;
import nablarch.core.util.DateUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.interceptor.OnError;
import nablarch.fw.web.upload.PartInfo;
import nablarch.fw.web.upload.util.UploadHelper;
import please.change.me.common.file.management.FileManagementUtil;

import java.sql.Blob;
import java.util.List;

/**
 * データベースを用いたファイル管理機能サンプル
 */
public class DbFileManagementAction {

    /**
     * 初期画面の表示。
     *
     * @param request HTTPリクエスト
     * @param context 実行コンテキスト
     * @return HTTPレスポンス
     */
    public HttpResponse index(HttpRequest request, ExecutionContext context) {
        return new HttpResponse("/WEB-INF/test/dbFileManagement.jsp");
    }

    /**
     * ファイルをアップロードし、DBに登録する。
     *
     * @param request HTTPリクエスト
     * @param context 実行コンテキスト
     * @return HTTPレスポンス
     */
    @OnDoubleSubmission
    @OnError(type = ApplicationException.class, path = "/WEB-INF/test/dbFileManagement.jsp")
    public HttpResponse upload(HttpRequest request, ExecutionContext context) {

        // アップロードファイルの取得
        List<PartInfo> partInfoList = request.getPart("uploadFile");
        if (partInfoList.isEmpty()) {
            return new HttpResponse("/WEB-INF/test/dbFileManagement.jsp");
        }
        PartInfo partInfo = partInfoList.get(0);

        // ファイルの登録
        // String fileId =
        FileManagementUtil.save(partInfo);

//
//        LoginUserPrincipal userContext = SessionUtil.get(context, "userContext");
//
//        List<Project> projects = readFileAndValidate(partInfo, userContext);
//
//        // DBへ一括登録する
//        insertProjects(projects);
//
//        // 完了メッセージの追加
//        context.setRequestScopedVar("uploadProjectSize", projects.size());
//
        // ファイルの保存
        saveFile(partInfo);

        return new HttpResponse("/WEB-INF/test/dbFileManagement.jsp");
    }

    /**
     * DBに登録したファイルをダウンロードする。
     *
     * @param request HTTPリクエスト
     * @param context 実行コンテキスト
     * @return HTTPレスポンス
     */
    @OnError(type = ApplicationException.class, path = "/WEB-INF/test/dbFileManagement.jsp")
    public HttpResponse download(HttpRequest request, ExecutionContext context) {

        //ダウンロードに使用するファイルID
        String fileId = "000000000000000019";

        // ファイルをDBから取得
        Blob blob = FileManagementUtil.find(fileId);

        // レスポンス情報を設定
        StreamResponse response = new StreamResponse(blob);
        response.setContentType("text/csv; charset=Shift_JIS");
        response.setContentDisposition("プロジェクト一覧.csv");

        return response;
    }

    /**
     * ファイルを保存する。
     *
     * @param partInfo アップロードファイルの情報
     */
    private void saveFile(final PartInfo partInfo) {
        String fileName = generateUniqueFileName(partInfo.getFileName());
        UploadHelper helper = new UploadHelper(partInfo);
        helper.moveFileTo("uploadFiles", fileName);
    }

    /**
     * 一意なファイル名を生成する。
     * 同時に同名のファイルがアップロードされることはないというシステム運用のもと、
     * ”ファイル名+アップロード時刻.csv” というファイル名を生成している。
     *
     * @param fileName ファイル名
     * @return 一意なファイル名
     */
    private String generateUniqueFileName(String fileName) {
        String fileNameWithoutExtension;
        String fileExtension = "";

        int lastDotPos = fileName.lastIndexOf('.');
        if (lastDotPos == -1 || lastDotPos == 0) {
            fileNameWithoutExtension = fileName;
        } else {
            fileNameWithoutExtension = fileName.substring(0, lastDotPos);
            fileExtension = '.' + fileName.substring(lastDotPos + 1);
        }

        String date = DateUtil.formatDate(SystemTimeUtil.getDate(), "yyMMddHHmmss");
        return fileNameWithoutExtension + '_' + date + fileExtension;
    }
}

<%--
TODO 疎通確認用のクラスです。確認完了後、ディレクトリごと削除してください。
--%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="n" uri="http://tis.co.jp/nablarch" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <title>データベースを用いたファイル管理機能サンプル</title>
</head>
<body>
<n:form useToken="true" enctype="multipart/form-data">
  <h4>ファイルアップロード</h4>
  <div class="input-group">
    <n:file name="uploadFile" id="uploadFile"/>
    <n:button uri="/action/dbFileManagement/upload" allowDoubleSubmission="false" cssClass="btn btn-raised btn-default">登録</n:button>
  </div>
</n:form>
<n:form useToken="true" enctype="multipart/form-data">
  <h4>ファイルダウンロード</h4>
  <div class="input-group">
    <n:button uri="/action/dbFileManagement/download" allowDoubleSubmission="false" cssClass="btn btn-raised btn-default">登録</n:button>
  </div>
</n:form>
</body>
</html>

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
<h1>データベースを用いたファイル管理機能サンプル</h1>
<h2>ファイルアップロード</h2>
<n:form useToken="true" enctype="multipart/form-data">
  <div class="input-group">
    <n:file name="uploadFile" id="uploadFile"/>
    <n:button uri="/action/dbFileManagement/upload">登録</n:button>
  </div>
</n:form>
<h2>ファイルアップロード</h2>
<n:form useToken="true" enctype="multipart/form-data">
  <c:if test="${searchResult != null}">
    <ul>
      <c:forEach items="${searchResult}" var="row">
        <li>
          <label>ファイルID：</label>
          <n:write name="row.fileControlId"/>
          <n:a href="/action/dbFileManagement/download/${row.fileControlId}">ダウンロード</n:a>
        </li>
      </c:forEach>
    </ul>
  </c:if>
</n:form>
</body>
</html>

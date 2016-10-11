package please.change.me.common.captcha;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import nablarch.core.db.statement.ResultSetConvertor;

/**
 * Oracleデータベース用のSELECT結果の項目値を変換するクラス。<br>
 * <br>
 * {@link java.sql.ResultSet#getObject(int)} を使用した場合のOracle JDBCドライバの下記問題を吸収するための{@link nablarch.core.db.statement.ResultSetConvertor}。<br>
 * 本クラスでは、これらの問題を解決するために、下記の問題点に該当するデータタイプの場合には、{@link java.sql.ResultSet#getTimestamp(int)} を使用してデータの取得を行う。<br>
 * <br>
 * 問題点
 * <ul>
 * <li>{@link java.sql.Types#TIMESTAMP}の場合は、oracle.sql.TIMESTAMPが返却されアプリケーションで{@link
 * java.sql.Timestamp}へ変換を行う必要がある。</li>
 * </ul>
 *
 * 拡張モジュールサンプルのベンダー依存のデータベース関連拡張サンプル（please.change.me.core.db.statement.OracleResultSetConvertor）より必要部分のみ抜粋。
 * 
 * @author TIS
 */
public class TestResultSetConvertor implements ResultSetConvertor {

    /** {@inheritDoc} */
    public Object convert(ResultSet rs, ResultSetMetaData rsmd, int columnIndex) throws SQLException {
        return rs.getTimestamp(columnIndex);
    }

    /**
     * {@inheritDoc}
     * 指定されたカラムのデータタイプが、
     * {@link java.sql.Types#TIMESTAMP}の場合には、trueを返却する。
     */
    public boolean isConvertible(ResultSetMetaData rsmd, int columnIndex) throws SQLException {
        return rsmd.getColumnType(columnIndex) == java.sql.Types.TIMESTAMP;
    }
}

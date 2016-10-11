package please.change.me.common.mail.html;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import nablarch.core.db.dialect.OracleDialect;
import nablarch.core.db.statement.ResultSetConvertor;
import nablarch.core.db.statement.SelectOption;

/**
 * Oracle用の方言を吸収するためのクラスです。
 *
 * @author Naoki Yamamoto
 */
public class TestOracleDialect extends OracleDialect {

    /** Query Timeアウト時に発生する例外のエラーコード */
    private static final int QUERY_CANCEL_ERROR_CODE = 1013;

    /** 検索結果の値変換クラス */
    private static final OracleResultSetConvertor RESULT_SET_CONVERTOR = new OracleResultSetConvertor();

    /**
     * {@inheritDoc}
     * <p/>
     * Oracleデータベースの場合、以下例外の場合タイムアウト対象の例外として扱う。
     * <ul>
     * <li>エラーコード:1013(クエリタイムアウト時に送出される例外)</li>
     * </ul>
     */
    @Override
    public boolean isTransactionTimeoutError(SQLException sqlException) {
        final int errorCode = sqlException.getErrorCode();
        return errorCode == QUERY_CANCEL_ERROR_CODE;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Oracle11g以前のバージョンでは、offset構文をサポートしていないが、
     * rownum擬似列を用いてoffset-limit同等のことが実現できるため、
     * {@code true}を返却する。
     */
    @Override
    public boolean supportsOffset() {
        return true;
    }

    /**
     * シーケンスはサポートする。
     *
     * @return true固定
     */
    @Override
    public boolean supportsSequence() {
        return true;
    }

    /**
     * シーケンスオブジェクトの次の値を取得するSQL文を構築する。
     *
     * @param sequenceName シーケンス名
     * @return シーケンスオブジェクトの次の値を取得するSQL文
     */
    @Override
    public String buildSequenceGeneratorSql(String sequenceName) {
        assert sequenceName != null;
        return "SELECT " + sequenceName + ".NEXTVAL FROM DUAL";
    }

    @Override
    public ResultSetConvertor getResultSetConvertor() {
        return RESULT_SET_CONVERTOR;
    }

    /**
     * SQL例外が一意制約違反による例外かどうか判定する。
     * <p/>
     * SQL例外が一意制約違反による例外の場合{@code true}<br />
     *
     * @param sqlException SQL例外
     * @return errorCode が {@literal 1}の場合true.
     */
    @Override
    public boolean isDuplicateException(SQLException sqlException) {
        return sqlException.getErrorCode() == 1;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * ページングの条件を元に、取得レコードをフィルタリングするSQLに変換する。
     * <p/>
     * Oracleでは、offsetやlimitはサポートされていないため(Oracle12c以降ではサポートはされている)、
     * rownum擬似列を使用して取得レコード数のフィルタリングを行うSQL文に変換を行う。
     */
    @Override
    public String convertPaginationSql(String sql, SelectOption selectOption) {
        boolean hasOffset = selectOption.getOffset() > 0;

        StringBuilder result = new StringBuilder(256);
        result.append("SELECT SUB2.* FROM (SELECT SUB1.*, ROWNUM ROWNUM_ FROM (")
                .append(sql)
                .append(") SUB1 ) SUB2 WHERE");
        if (hasOffset) {
            result.append(" SUB2.ROWNUM_ > ")
                    .append(selectOption.getOffset());
        }

        if (selectOption.getLimit() > 0) {
            if (hasOffset) {
                result.append(" AND SUB2.ROWNUM_ <= ")
                        .append(selectOption.getOffset() + selectOption.getLimit());
            } else {
                result.append(" SUB2.ROWNUM_ <= ")
                        .append(selectOption.getLimit());
            }
        }
        return result.toString();
    }

    /**
     * ResultSetから値を取得するクラス。
     */
    private static class OracleResultSetConvertor implements ResultSetConvertor {

        @Override
        public Object convert(ResultSet rs, ResultSetMetaData rsmd, int columnIndex) throws SQLException {
            switch (rsmd.getColumnType(columnIndex)) {
                case Types.NUMERIC:
                    return getNumberType(rs, rsmd, columnIndex);
                case Types.TIMESTAMP:
                    return rs.getTimestamp(columnIndex);
                case Types.DATE:
                    return rs.getTimestamp(columnIndex);
                case Types.CLOB:
                case Types.NCLOB:
                    return rs.getString(columnIndex);
                default:
                    return rs.getObject(columnIndex);
            }
        }

        /**
         * 数値型のカラムの取得を行う。
         * <p/>
         * 小数部有りの場合には、{@link java.math.BigDecimal}で取得する。
         * 小数部なしで、桁数が9桁以下の場合には、{@link Integer}で取得する。
         * 上記に該当しない場合には、{@link Long}で取得する。
         *
         * @param rs {@link ResultSet}
         * @param rsmd {@link ResultSetMetaData}
         * @param columnIndex カラムインデックス
         * @return 数値型カラムの値
         * @throws SQLException データベース関連の例外
         */
        private static Number getNumberType(ResultSet rs, ResultSetMetaData rsmd, int columnIndex) throws SQLException {
            if (rsmd.getScale(columnIndex) == 0) {
                // 少数部なしのカラム
                if (rsmd.getPrecision(columnIndex) <= 9) {
                    final int asInt = rs.getInt(columnIndex);
                    if (rs.wasNull()) {
                        return null;
                    }
                    return asInt;
                } else {
                    final long asLong = rs.getLong(columnIndex);
                    if (rs.wasNull()) {
                        return null;
                    }
                    return asLong;
                }
            } else {
                return rs.getBigDecimal(columnIndex);
            }
        }

        @Override
        public boolean isConvertible(ResultSetMetaData rsmd, int columnIndex) throws SQLException {
            return true;
        }
    }

    @Override
    public String getPingSql() {
        return "select 1 from dual";
    }
}

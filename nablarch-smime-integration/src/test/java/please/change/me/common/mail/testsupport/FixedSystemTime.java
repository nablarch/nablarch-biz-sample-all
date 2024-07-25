package please.change.me.common.mail.testsupport;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import nablarch.core.date.SystemTimeProvider;

/**
 * テストで使用するシステムタイム
 *
 * @author hisaaki sioiri
 */
public class FixedSystemTime implements SystemTimeProvider {

    /** タイムスタンプ */
    private static Timestamp timestamp;

    /** フォーマット */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddhhmmssSSS");

    /**
     * Dateを返す。
     * @return Date
     */
    public Date getDate() {
        return timestamp;
    }

    /**
     * タイムスタンプを返す。
     * @return タイムスタンプ
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * テストで使用する日付を設定する。
     * @param timestampStr
     * @throws ParseException
     */
    public static void setTimestamp(String timestampStr) throws ParseException {
        timestamp = new Timestamp(DATE_FORMAT.parse(timestampStr).getTime());
    }
}

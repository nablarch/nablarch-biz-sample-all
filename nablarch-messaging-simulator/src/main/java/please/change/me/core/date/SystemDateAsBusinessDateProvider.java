package please.change.me.core.date;

import nablarch.core.date.BusinessDateProvider;
import nablarch.core.date.SystemTimeUtil;

import java.util.Map;

/**
 * システム日付を提供するクラス<br>
 * @author Kohei Sawaki
 * @since 1.4.2
 */
public class SystemDateAsBusinessDateProvider implements BusinessDateProvider {

    @Override
    public String getDate() {
        return SystemTimeUtil.getDateString();
    }

    @Override
    public String getDate(String segment) {
        return SystemTimeUtil.getDateString();
    }

    @Override
    public Map<String, String> getAllDate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDate(String segment, String date) {
        throw new UnsupportedOperationException();
    }
}

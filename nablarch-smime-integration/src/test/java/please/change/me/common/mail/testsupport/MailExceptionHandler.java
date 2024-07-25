package please.change.me.common.mail.testsupport;

import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.results.TransactionAbnormalEnd;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * コメント。
 *
 * @author hisaaki sioiri
 */
public class MailExceptionHandler implements Handler<Object, Object> {

    public static Exception exception;

    public Object handle(Object o, ExecutionContext context) {
        try {
            return context.handleNext(o);
        } catch (RuntimeException e) {
            exception = e;
            throw e;
        }
    }

    public static void assertException(int exitCode, String failureCode, String msg) {
        assertThat(exception, is(instanceOf(TransactionAbnormalEnd.class)));
        TransactionAbnormalEnd abnormalEnd = (TransactionAbnormalEnd) exception;

        assertThat(abnormalEnd.getStatusCode(), is(exitCode));
        assertThat(abnormalEnd.getMessageId(), is(failureCode));
        assertThat(abnormalEnd.getMessage(), is(containsString(msg)));
    }
}

package please.change.me;

import nablarch.test.core.messaging.MessagingRequestTestSupport;
import nablarch.test.junit5.extension.messaging.MessagingRequestTest;
import org.junit.jupiter.api.Test;

/**
 * {@link ReceiveFormUrlEncodedAction}のテストクラス。
 *
 */
@MessagingRequestTest
class ReceiveFormUrlEncodedTest {
    MessagingRequestTestSupport support;

    /** 正常終了のテストケース。 */
    @Test
    void testNormalEnd() {
        support.execute(support.testName.getMethodName());
    }
}

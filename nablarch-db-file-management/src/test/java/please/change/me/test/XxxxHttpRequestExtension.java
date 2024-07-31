package please.change.me.test;

import nablarch.test.event.TestEventDispatcher;
import nablarch.test.junit5.extension.http.BasicHttpRequestTestExtension;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link XxxxHttpRequestTestSupport}をテストで使用できるようにするためのExtensionクラス。
 */
// TODO XxxxをPJ名に変更してください(例:MyProjectHttpRequestExtension)。
public class XxxxHttpRequestExtension extends BasicHttpRequestTestExtension {

    @Override
    protected TestEventDispatcher createSupport(Object testInstance, ExtensionContext context) {
        XxxxHttpRequestTest annotation = findAnnotation(testInstance, XxxxHttpRequestTest.class);
        return new XxxxHttpRequestTestSupport(testInstance.getClass(), annotation.baseUri());
    }
}

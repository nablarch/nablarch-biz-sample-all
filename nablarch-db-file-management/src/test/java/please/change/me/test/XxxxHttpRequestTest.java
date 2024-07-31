package please.change.me.test;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link XxxxHttpRequestExtension}をテストクラスに適用するための合成アノテーション。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(XxxxHttpRequestExtension.class)
// TODO XxxxをPJ名に変更してください(例:MyProjectHttpRequestTest)。
public @interface XxxxHttpRequestTest {
    /**
     * ベースURI。
     * @return ベースURI
     */
    String baseUri();
}

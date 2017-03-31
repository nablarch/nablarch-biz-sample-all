package please.change.me.core.validation.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nablarch.core.validation.Validation;

/**
 * メールアドレスであることを表すアノテーション。<br>
 *
 * @author Tomokazu Kagawa
 */
@Validation
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MailAddress {

    /**
     * メールアドレス精査エラーの場合のメッセージID。<br/>
     * 指定しなかった場合、デフォルトが使用される。
     */
    String messageId() default "";
}

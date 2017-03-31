package please.change.me.core.validation.validator;

import java.lang.annotation.Annotation;
import java.util.Map;

import nablarch.core.util.StringUtil;
import nablarch.core.validation.DirectCallableValidator;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationResultMessageUtil;

/**
 * メールアドレスが有効であるかをチェックするバリデータクラス。
 * 
 * @author Tomokazu Kagawa
 */
public class MailAddressValidator implements DirectCallableValidator {

    /**
     * デフォルトのエラーメッセージのメッセージID。
     */
    private String messageId;

    /**
     * デフォルトのエラーメッセージのメッセージIDを設定する。<br/>
     * 例 : "{0}は有効なメールアドレスではありません。"
     * 
     * @param messageId
     *            エラーメッセージのデフォルトのメッセージID
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * {@inheritDoc}
     */
    public Class<? extends Annotation> getAnnotationClass() {
        return MailAddress.class;
    }

    /**
     * {@inheritDoc}
     */
    public <T> boolean validate(ValidationContext<T> context,
            String propertyName, Object propertyDisplayName,
            Annotation annotation, Object value) {

        String localMessageId = messageId;
        MailAddress mailAddress = (MailAddress) annotation;
        if (!StringUtil.isNullOrEmpty(mailAddress.messageId())) {
            localMessageId = mailAddress.messageId();
        }

        if (value == null) {
            return true;
        } else if (!(value instanceof String)) {
            ValidationResultMessageUtil.addResultMessage(context, propertyName,
                                                        localMessageId, propertyDisplayName);
            return false;
        } else if ("".equals(value)) {
            return true;
        }

        if (!VariousValidationUtil.isValidMailAddress((String) value)) {
            ValidationResultMessageUtil.addResultMessage(context, propertyName,
                    localMessageId, propertyDisplayName);
            return false;
        }

        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public <T> boolean validate(ValidationContext<T>      context,
                                String                    propertyName,
                                Object                    propertyDisplayName,
                                final Map<String, Object> params,
                                Object                    value) {
        
        MailAddress annotation = new MailAddress() {
            public Class<? extends Annotation> annotationType() {
                return MailAddress.class;
            }
            public String messageId() {
                String messageId = (String) params.get("messageId");
                return (messageId == null) ? ""
                                           : messageId;
            }
        };
    
        return validate(context, propertyName, propertyDisplayName, annotation, value);
    }
}

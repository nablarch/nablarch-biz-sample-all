package please.change.me.core.validation.validator;

import java.lang.annotation.Annotation;
import java.util.Map;

import nablarch.core.util.StringUtil;
import nablarch.core.validation.DirectCallableValidator;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationResultMessageUtil;

/**
 * 日本の電話番号が有効であることをチェックするバリデータクラス。
 * 
 * @author Tomokazu Kagawa
 */
public class JapaneseTelNumberValidator implements DirectCallableValidator {

    /**
     * デフォルトのエラーメッセージのメッセージID。
     */
    private String messageId;

    /**
     * デフォルトのエラーメッセージのメッセージIDを設定する。<br/>
     * 例 : "{0}は有効な電話番号ではありません。"
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
        return JapaneseTelNumber.class;
    }

    /**
     * {@inheritDoc}
     */
    public <T> boolean validate(ValidationContext<T> context,
            String propertyName, Object propertyDisplayName,
            Annotation annotation, Object value) {

        if (value == null) {
            return true;
        } else if (!(value instanceof String)) {
            throw new IllegalArgumentException(
                    "value isn't instance of String. " + "property name = "
                            + propertyName + "," + " property message id  = "
                            + propertyDisplayName + "," + " property type = "
                            + value.getClass().getName());
        }

        String localMessageId = messageId;
        JapaneseTelNumber japaneseTelNumber = (JapaneseTelNumber) annotation;
        if (!StringUtil.isNullOrEmpty(japaneseTelNumber.messageId())) {
            localMessageId = japaneseTelNumber.messageId();
        }

        if (!VariousValidationUtil.isValidJapaneseTelNum((String) value)) {
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
        
        JapaneseTelNumber annotation = new JapaneseTelNumber() {
            public Class<? extends Annotation> annotationType() {
                return JapaneseTelNumber.class;
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

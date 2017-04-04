package please.change.me.core.validation.creator;

import java.lang.reflect.Constructor;
import java.util.Map;

import nablarch.core.util.ObjectUtil;
import nablarch.core.validation.FormCreator;
import nablarch.core.validation.FormValidationDefinition;


/**
 * デフォルトコンストラクタでフォームを生成し、リフレクションを使用してプロパティをセットするクラス。
 * 
 * @author Koichi Asano
 *
 */
public class ReflectionFormCreator implements FormCreator {

    /**
     * {@inheritDoc}
     */
    public <T> T create(Class<T> targetClass,
            Map<String, Object> propertyValues, FormValidationDefinition formValidationDefinition) {
        
        try {
            Constructor<T> constructor = targetClass.getConstructor();
            T form = constructor.newInstance();
            for (Map.Entry<String, Object> entry : propertyValues.entrySet()) {
                String propertyName = entry.getKey();
                Object value = entry.getValue(); 
                ObjectUtil.getSetterMethod(targetClass, propertyName).invoke(form, value);
            }
            return form;
        } catch (Exception e) {
            // 通常ここには到達しません。
            throw new IllegalArgumentException("Entity creation failed. " 
                    + "form class name = [" + targetClass.getName() + "] .", e);
        }
    }
}

package please.change.me.core.dataformat.convertor;

import java.util.Map;

import nablarch.core.dataformat.convertor.ConvertorFactorySupport;
import nablarch.core.dataformat.convertor.datatype.NullableString;
import nablarch.core.dataformat.convertor.value.CharacterReplacer;
import nablarch.core.dataformat.convertor.value.DefaultValue;
import nablarch.core.dataformat.convertor.value.NumberString;
import nablarch.core.dataformat.convertor.value.SignedNumberString;
import nablarch.core.util.map.CaseInsensitiveMap;

/**
 * KeyValue形式データコンバータのファクトリクラス。
 * 
 * @author TIS
 */
public class FormUrlEncodedDataConvertorFactory extends ConvertorFactorySupport {

    /**
     * KeyValue形式データのデフォルトのコンバータ名とコンバータ実装クラスの対応表を返却する。
     * @return KeyValue形式データのデフォルトのコンバータ名とコンバータ実装クラスの対応表
     */
    @Override
    protected Map<String, Class<?>> getDefaultConvertorTable() {
        return DEFAULT_CONVERTOR_TABLE;
    }
    
    /** デフォルトのコンバータ名とコンバータ実装クラスの対応表*/
    private static final Map<String, Class<?>>
    DEFAULT_CONVERTOR_TABLE = new CaseInsensitiveMap<Class<?>>() {
        {
            // ------------------------------ DataType
            put("X",         NullableString.class);
            put("N",         NullableString.class);
            put("XN",        NullableString.class);
            put("X9",        NullableString.class);
            put("SX9",       NullableString.class);
            // ------------------------------ ValueConvertor
            put("_LITERAL_", DefaultValue.class);
            put("number",        NumberString.class);
            put("signed_number", SignedNumberString.class);
            put("replacement", CharacterReplacer.class);
        }
    };
}

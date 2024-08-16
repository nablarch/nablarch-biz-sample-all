package please.change.me.core.dataformat.convertor;

import nablarch.core.dataformat.convertor.FixedLengthConvertorFactory;
import nablarch.core.util.map.CaseInsensitiveMap;
import please.change.me.core.dataformat.convertor.datatype.EbcdicDoubleByteCharacterString;
import please.change.me.core.dataformat.convertor.datatype.EbcdicNoShiftCodeDoubleByteCharacterString;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EBCDIC(CP930)用のデータタイプを追加した固定長フォーマット用のコンバータファクトリ。
 */
public class EbcdicFixedLengthConvertorFactory extends FixedLengthConvertorFactory {
    @Override
    protected Map<String, Class<?>> getDefaultConvertorTable() {
        final Map<String, Class<?>> defaultConvertorTable = new CaseInsensitiveMap<Class<?>>(
                new ConcurrentHashMap<String, Class<?>>(super.getDefaultConvertorTable()));
        // EBCDIC(CP930)用のデータタイプ ESN, EN を追加する
        defaultConvertorTable.put("ESN", EbcdicDoubleByteCharacterString.class);
        defaultConvertorTable.put("EN", EbcdicNoShiftCodeDoubleByteCharacterString.class);
        return Collections.unmodifiableMap(defaultConvertorTable);
    }
}
package please.change.me.core.dataformat.convertor;

import static org.junit.Assert.assertSame;

import java.util.Map;

import nablarch.core.dataformat.convertor.datatype.Bytes;
import nablarch.core.dataformat.convertor.datatype.CharacterStreamDataString;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.ComponentDefinitionLoader;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;

import org.junit.After;
import org.junit.Test;

/**
 * XmlDataConvertorのテスト。
 * 
 * 観点：
 * デフォルト設定での動作は別のテストで確認しているので、ここではリポジトリから設定できることのテストを行う。
 * 
 * @author TIS
 */
public class FormUrlEncodedDataConvertorSettingTest {

    @After
    public void tearDown() throws Exception {
        SystemRepository.clear();
    }
    
    /**
     * コンバータをリポジトリから設定するテスト
     */
    @Test
    public void testRepository() throws Exception {
        // テスト用のリポジトリ構築
        ComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "please/change/me/core/dataformat/convertor/ConvertorSetting.xml");
        DiContainer container = new DiContainer(loader);
        SystemRepository.clear();
        SystemRepository.load(container);

        FormUrlEncodedDataConvertorSetting setting = FormUrlEncodedDataConvertorSetting.getInstance();
        Map<String, Class<?>> resultTable = setting.getConvertorFactory().getConvertorTable();
        assertSame(CharacterStreamDataString.class, resultTable.get("Test"));
        assertSame(Bytes.class, resultTable.get("Hoge"));
        
        // デフォルトのリポジトリに戻す
        loader = new XmlComponentDefinitionLoader(
                "please/change/me/core/dataformat/convertor/DefaultConvertorSetting.xml");
        container = new DiContainer(loader);
        SystemRepository.clear();
        SystemRepository.load(container);
    }
}

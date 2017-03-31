package please.change.me.core.dataformat.convertor;

import java.util.Map;

import nablarch.core.dataformat.convertor.ConvertorFactorySupport;
import nablarch.core.dataformat.convertor.ConvertorSetting;
import nablarch.core.repository.SystemRepository;

/**
 * KeyValue形式の読み書きを行う際に使用するコンバータの設定情報を保持するクラス。
 * コンバータ名とコンバータ実装クラスの対応表 や、タイトルのレコードタイプ名などを、DIコンテナから設定できる。
 * 
 * @author TIS
 */
public class FormUrlEncodedDataConvertorSetting implements ConvertorSetting {

    /** コンバータのファクトリクラス */
    private static FormUrlEncodedDataConvertorFactory factory = new FormUrlEncodedDataConvertorFactory();

    /** システムリポジトリ上の登録名 */
    private static final String REPOSITORY_KEY =  "formUrlEncodedDataConvertorSetting";

    /**
     * デフォルトのコンバータ設定情報保持クラスのインスタンス。
     * リポジトリからインスタンスを取得できなかった場合に、デフォルトでこのインスタンスが使用される。
     */
    private static final FormUrlEncodedDataConvertorSetting DEFAULT_SETTING = new FormUrlEncodedDataConvertorSetting();

    /**
     * このクラスのインスタンスをリポジトリより取得する。
     * リポジトリにインスタンスが存在しない場合は、デフォルトの設定で生成したこのクラスのインスタンスを返却する。
     * @return このクラスのインスタンス
     */
    public static FormUrlEncodedDataConvertorSetting getInstance() {
        FormUrlEncodedDataConvertorSetting setting = 
                SystemRepository.get(REPOSITORY_KEY);
        if (setting == null) {
            return DEFAULT_SETTING;
        }
        return setting;
    }
    
    /**
     * コンバータのファクトリを返却する。
     * @return コンバータのファクトリ
     */
    public ConvertorFactorySupport getConvertorFactory() {
        return factory;
    }
    
    /**
     * デフォルトのコンバータ名とコンバータ実装クラスの対応表を設定する。
     * @param table コンバータ名と、コンバータの実装クラスを保持するテーブル
     * @return このオブジェクト自体
     * @throws ClassNotFoundException 指定されたクラスが存在しなかった場合、
     * もしくは、指定されたクラスが ValueConvertorを実装していなかった場合に、スローされる例外
     */
    public ConvertorSetting setConvertorTable(Map<String, String> table) throws ClassNotFoundException {
        factory.setConvertorTable(table);
        return this;
    }
    
}

package please.change.me.core.validation.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import nablarch.common.code.Code;
import nablarch.core.cache.StaticDataLoader;
import nablarch.core.repository.initialization.Initializable;
import nablarch.core.util.I18NUtil;

/**
 * コードをMapとして保持するモッククラス。
 */
public class MockCodeLoader implements StaticDataLoader<Code>, Initializable {

    /**
     * パターンカラム名
     */
    private String[] patternColumnNames = new String[] {"PATTERN01", "PATTERN02", "PATTERN03"};

    /**
     * オプション名称のカラム名
     */
    private String[] optionNameColumnNames = new String[] {"NAME_WITH_VALUE", "OPTION01"};

    private List<Code> codeList;

    private List<CodePattern> patterns;

    private List<CodeName> names;

    public void setPatterns(List<CodePattern> patterns) {
        this.patterns = patterns;
    }

    public void setNames(List<CodeName> names) {
        this.names = names;
    }

    @Override
    public Code getValue(Object id) {
        for (Code code : codeList) {
            if (code instanceof BasicCode) {
                if (((BasicCode) code).codeId.equals(id)) {
                    return code;
                }
            }
        }
        return null;
    }

    @Override
    public List<Code> getValues(String indexName, Object key) {
        return null;
    }

    @Override
    public List<Code> loadAll() {
        return codeList;
    }

    @Override
    public List<String> getIndexNames() {
        return null;
    }

    @Override
    public Object getId(Code value) {
        return value.getCodeId();
    }

    @Override
    public Object generateIndexKey(String indexName, Code value) {
        return null;
    }

    @Override
    public void initialize() {
        if (patterns == null || names == null) {
            return;
        }
        codeList = new ArrayList<Code>();
        List<CodeInfo> codeInfoList = new ArrayList<CodeInfo>();
        String id = patterns.get(0).id;
        for (CodePattern pattern : patterns) {
            if (!pattern.id.equals(id)) {
                sort(codeInfoList);
                codeList.add(new BasicCode(id, codeInfoList));
                id = pattern.id;
                codeInfoList.clear();
            }
            for (CodeName name : names) {
                if (pattern.id.equals(name.id) && pattern.value.equals(name.value)) {
                    codeInfoList.add(new CodeInfo(pattern, name));
                }
            }
        }
        if (!codeInfoList.isEmpty()) {
            sort(codeInfoList);
            codeList.add(new BasicCode(id, codeInfoList));
        }
    }

    private void sort(List<CodeInfo> list) {
        Collections.sort(list, new Comparator<CodeInfo>() {
            @Override
            public int compare(CodeInfo o1, CodeInfo o2) {
                int comp = o1.getLang()
                             .compareTo(o2.getLang());
                if (comp != 0) {
                    return comp;
                }
                long result = o1.getSortOrder() - o2.getSortOrder();
                if (result > 0) {
                    return 1;
                } else if (result < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }

    private final class CodeInfo {

        private CodePattern codePattern;

        private CodeName codeName;

        public CodeInfo(CodePattern pattern, CodeName name) {
            if (!(pattern.id.equals(name.id) && pattern.value.equals(name.value))) {
                throw new IllegalArgumentException("not match");
            }
            codePattern = pattern;
            codeName = name;
        }

        public String getLang() {
            return codeName.lang;
        }

        public String getName() {
            return codeName.name;
        }

        public String getValue() {
            return codeName.value;
        }

        public String getShortName() {
            return codeName.shortName;
        }

        public Long getSortOrder() {
            return codeName.sortOrder;
        }

        public String getString(String propName) {
            if ("PATTERN01".equals(propName)) {
                return codePattern.pattern01;
            }
            if ("PATTERN02".equals(propName)) {
                return codePattern.pattern02;
            }
            if ("PATTERN03".equals(propName)) {
                return codePattern.pattern03;
            }
            if ("OPTION01".equals(propName)) {
                return codeName.option01;
            }
            return null;
        }
    }

    /**
     * ロードするコードの実装。
     */
    private final class BasicCode implements Code {

        /**
         * コンストラクタ。
         *
         * @param codeId コードID
         * @param data コードを構成するデータのList。<br/>
         * データは言語を第1の条件としてソートされている必要がある。
         */
        private BasicCode(String codeId, List<CodeInfo> data) {

            this.codeId = codeId;

            parLangValuesMap = new HashMap<Locale, PerLangValues>();

            List<CodeInfo> langData = new ArrayList<CodeInfo>();
            String lang = "";

            for (CodeInfo row : data) {
                String currentLang = row.getLang();
                if (!lang.equals(currentLang)) {
                    if (langData.size() > 0) {
                        PerLangValues value = new PerLangValues(langData);
                        parLangValuesMap.put(I18NUtil.createLocale(lang), value);
                        langData.clear();
                    }

                    lang = currentLang;
                }
                langData.add(row);
            }

            PerLangValues value = new PerLangValues(langData);
            parLangValuesMap.put(I18NUtil.createLocale(lang), value);

            // containsの情報は、全言語分取得
            values = new HashSet<String>();
            patternValuesMap = new HashMap<String, Set<String>>();
            for (PerLangValues parLangValues : parLangValuesMap.values()) {
                values.addAll(parLangValues.values);

                for (String pattern : patternColumnNames) {
                    if (!patternValuesMap.containsKey(pattern)) {
                        patternValuesMap.put(pattern, new HashSet<String>());
                    }
                    patternValuesMap.get(pattern)
                                    .addAll(parLangValues.patternMap.get(pattern));
                }
            }
        }

        /**
         * コードID
         */
        private final String codeId;

        /**
         * 言語と言語毎に持つ値のMap
         */
        private final Map<Locale, PerLangValues> parLangValuesMap;

        /**
         * コードに含まれるコード値のセット
         */
        private final Set<String> values;

        /**
         * パターンに含まれる値のセット
         */
        private final Map<String, Set<String>> patternValuesMap;

        /**
         * {@inheritDoc}
         */
        public String getCodeId() {
            return codeId;
        }

        /**
         * {@inheritDoc}
         */
        public boolean contains(String value) {
            return values.contains(value);
        }

        /**
         * {@inheritDoc}
         */
        public boolean contains(String pattern, String value) {
            Set<String> patternValues = patternValuesMap.get(pattern);
            if (patternValues == null) {
                throw new IllegalArgumentException("pattern was not found. "
                        + "code id = " + codeId
                        + ", pattern = " + pattern);
            }
            return patternValues.contains(value);
        }

        /**
         * {@inheritDoc}
         */
        public String getName(String value, Locale locale) {
            PerLangValues perLangValues = parLangValuesMap.get(locale);
            if (perLangValues == null) {
                throw new IllegalArgumentException("locale was not found. "
                        + "code id = " + codeId
                        + ", locale = " + locale);
            }
            String name = perLangValues.names.get(value);
            if (name == null) {
                throw new IllegalArgumentException("name was not found. "
                        + "code id = " + codeId
                        + ", locale = " + locale
                        + ", value = " + value);
            }
            return name;
        }

        /**
         * {@inheritDoc}
         */
        public String getShortName(String value, Locale locale) {
            PerLangValues parLangValues = parLangValuesMap.get(locale);
            if (parLangValues == null) {
                throw new IllegalArgumentException("locale was not found. "
                        + "code id = " + codeId
                        + ", locale = " + locale);
            }
            String shortName = parLangValues.shortNames.get(value);
            if (shortName == null) {
                throw new IllegalArgumentException("short name was not found. "
                        + "code id = " + codeId
                        + ", locale = " + locale
                        + ", value = " + value);
            }
            return shortName;
        }

        /**
         * {@inheritDoc}
         */
        public String getOptionalName(String value, String optionColumnName,
                Locale locale) {
            PerLangValues parLangValues = parLangValuesMap.get(locale);
            if (parLangValues == null) {
                throw new IllegalArgumentException("locale was not found. "
                        + "code id = " + codeId
                        + ", locale = " + locale);
            }
            Map<String, String> valueMap = parLangValues.optionNamesMap.get(optionColumnName);

            if (valueMap == null) {
                throw new IllegalArgumentException("option name was not found. "
                        + "code id = " + codeId
                        + ", locale = " + locale
                        + ", value = " + value);
            }
            String optionName = valueMap.get(value);
            if (optionName == null) {
                throw new IllegalArgumentException("option name was not found. "
                        + "code id = " + codeId
                        + ", locale = " + locale
                        + ", value = " + value
                        + ", option name = " + optionColumnName);
            }
            return optionName;
        }

        /**
         * {@inheritDoc}
         */
        public List<String> getValues(Locale locale) {
            PerLangValues parLangValues = parLangValuesMap.get(locale);
            if (parLangValues == null) {
                throw new IllegalArgumentException("locale was not found. "
                        + "code id = " + codeId
                        + ", locale = " + locale);
            }
            return parLangValues.values;
        }

        /**
         * {@inheritDoc}
         */
        public List<String> getValues(String pattern, Locale locale) {
            PerLangValues parLangValues = parLangValuesMap.get(locale);
            if (parLangValues == null) {
                throw new IllegalArgumentException("locale was not found. "
                        + "code id = " + codeId
                        + ", locale = " + locale);
            }
            List<String> values = parLangValues.patternMap.get(pattern);
            if (values == null) {
                throw new IllegalArgumentException("pattern was not found. "
                        + "code id = " + codeId
                        + ", locale = " + locale
                        + ", pattern = " + pattern);
            }
            return values;
        }
    }

    /**
     * 言語毎に持つ値を保持するクラス。
     */
    private final class PerLangValues {

        /**
         * 全てのコード値のList。
         */
        private final List<String> values;

        /**
         * パターン毎のコード値のListを保持するMap。
         */
        private final Map<String, List<String>> patternMap;

        /**
         * 名称を保持するMap。<br/>
         * <br/>
         * key:コード値<br/>
         * value:コード名称<br/>
         */
        private final Map<String, String> names;

        /**
         * 略称を保持するMap。<br/>
         * <br/>
         * key:コード値<br/>
         * value:コードの略称<br/>
         */
        private final Map<String, String> shortNames;

        /**
         * コードのオプション名称を保持するMap。<br/>
         * <br/>
         * key:コード値<br/>
         * value:コードのオプション名称<br/>
         */
        private final Map<String, Map<String, String>> optionNamesMap;


        /**
         * コンストラクタ。
         *
         * @param data コードを構成するデータのList。<br/>
         * データはソート順を第1の条件としてソートされている必要がある。
         */
        private PerLangValues(List<CodeInfo> data) {

            List<String> tmpValues = new ArrayList<String>();

            Map<String, String> tmpNames = new HashMap<String, String>();
            Map<String, String> tmpShortNames = new HashMap<String, String>();

            Map<String, Map<String, String>> tmpOptionNamesMap = new HashMap<String, Map<String, String>>();
            for (String optionName : optionNameColumnNames) {
                tmpOptionNamesMap.put(optionName, new HashMap<String, String>());
            }

            Map<String, List<String>> tmpPatternMap = new HashMap<String, List<String>>();
            for (String patternName : patternColumnNames) {
                tmpPatternMap.put(patternName, new ArrayList<String>());
            }

            for (CodeInfo row : data) {
                String value = row.getValue();
                tmpValues.add(value);
                tmpNames.put(value, row.getName());
                tmpShortNames.put(value, row.getShortName());

                for (String optionName : optionNameColumnNames) {
                    tmpOptionNamesMap.get(optionName)
                                     .put(value, row.getString(optionName));
                }

                for (String patternName : patternColumnNames) {

                    String patternIsValid = row.getString(patternName);
                    if ("1".equals(patternIsValid)) {
                        tmpPatternMap.get(patternName)
                                     .add(value);
                    }
                }

            }

            for (Map.Entry<String, Map<String, String>> entry : tmpOptionNamesMap.entrySet()) {
                entry.setValue(Collections.unmodifiableMap(entry.getValue()));
            }

            for (Map.Entry<String, List<String>> entry : tmpPatternMap.entrySet()) {
                entry.setValue(Collections.unmodifiableList(entry.getValue()));
            }

            this.values = Collections.unmodifiableList(tmpValues);
            this.names = Collections.unmodifiableMap(tmpNames);
            this.shortNames = Collections.unmodifiableMap(tmpShortNames);
            this.optionNamesMap = Collections.unmodifiableMap(tmpOptionNamesMap);
            this.patternMap = tmpPatternMap;
        }

    }
}
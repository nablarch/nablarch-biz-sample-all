package please.change.me.core.validation.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import please.change.me.core.validation.validator.entity.CodeName;
import please.change.me.core.validation.validator.entity.CodePattern;

import nablarch.common.code.validator.CodeValue;
import nablarch.core.ThreadContext;
import nablarch.core.message.Message;
import nablarch.core.message.MessageNotFoundException;
import nablarch.core.message.StringResource;
import nablarch.core.message.StringResourceHolder;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.ComponentDefinitionLoader;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.validation.ValidateFor;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationUtil;
import nablarch.test.support.db.helper.DatabaseTestRunner;
import nablarch.test.support.db.helper.VariousDbTestHelper;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * {@link CodeValidationUtil}のテストクラス
 *
 * @author hisaaki sioiri
 */
@RunWith(DatabaseTestRunner.class)
public class CodeValidationUtilTest {

    /**
     * テーブルとテストデータをセットアップし、設定ファイルをロードする。
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setupClass() throws Exception {
        createTable();

        setupTestData();

        ComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "please/change/me/core/validation/code-validation-util-test.xml");
        SystemRepository.load(new DiContainer(loader));
        ThreadContext.setLanguage(Locale.JAPANESE);
    }

    /**
     * テストで使用するコードテーブルを作成する。
     */
    private static void createTable() {
        VariousDbTestHelper.createTable(CodeName.class);
        VariousDbTestHelper.createTable(CodePattern.class);
    }

    /**
     * テストで使用するデータをセットアップする。
     */
    private static void setupTestData() {
        VariousDbTestHelper.setUpTable(
                new CodeName("C00001","01","ja",1,"コード１"),
                new CodeName("C00001","02","ja",2,"コード２")
        );
        VariousDbTestHelper.setUpTable(
                new CodePattern("C00001", "01", "1", "1", "0"),
                new CodePattern("C00001", "02", "1", "0", "0")
        );
    }

    /**
     * 後処理。
     *
     * @throws Exception
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        VariousDbTestHelper.dropTable(CodeName.class);
        VariousDbTestHelper.dropTable(CodePattern.class);
        SystemRepository.clear();
        ThreadContext.clear();
    }

    /**
     * 精査OKとなる場合のケース
     *
     * 精査エラーは発生せずに、Entityに変換出来る。
     */
    @Test
    public void testValidValue() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("codeValue", "01");

        ValidationContext<Entity> context = ValidationUtil.validateAndConvertRequest(Entity.class, params, "pattern1");
        assertThat(context.isValid(), is(true));

        Entity entity = context.createObject();
        assertThat(entity.getCodeValue(), is("01"));
    }

    /**
     * 精査NGとなる場合のケース
     *
     * 精査でエラーが発生すること。
     */
    @Test
    public void testIsInvalid() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("codeValue", "02");

        ValidationContext<Entity> context = ValidationUtil.validateAndConvertRequest(Entity.class, params, "pattern2");
        assertThat(context.isValid(), is(false));

        List<Message> messages = context.getMessages();
        assertThat(messages.size(), is(1));
        assertThat(messages.get(0).getMessageId(), is("MSG90002"));
    }

    /**
     * ベースエンティティ側で精査エラーが発生した場合のケース
     *
     * ベースエンティティで発生したエラーが1件だけメッセージに格納されていること
     */
    @Test
    public void testBaseInvalid() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("codeValue", "03");

        ValidationContext<Entity> context = ValidationUtil.validateAndConvertRequest(Entity.class, params, "pattern1");
        assertThat(context.isValid(), is(false));

        List<Message> messages = context.getMessages();
        assertThat(messages.size(), is(1));
        assertThat(messages.get(0).getMessageId(), is("MSG90002"));
    }

    /**
     * ベースエンティティ側で精査エラーが発生した場合のケース
     *
     * ベースエンティティで発生したエラーが1件だけメッセージに格納されていること
     */
    @Test
    public void testSpecifiesMessageId() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("codeValue", "02");

        ValidationContext<Entity> context = ValidationUtil.validateAndConvertRequest(Entity.class, params, "pattern3");
        assertThat(context.isValid(), is(false));

        List<Message> messages = context.getMessages();
        assertThat(messages.size(), is(1));
        assertThat(messages.get(0).getMessageId(), is("message_id"));
    }

    /**
     * テスト用のベースエンティティ
     */
    public static class BaseEntity {

        private String codeValue;

        public BaseEntity(Map<String, Object> params) {
            codeValue = (String) params.get("codeValue");
        }

        @CodeValue(codeId = "C00001")
        public void setCodeValue(String codeValue) {
            this.codeValue = codeValue;
        }

        public String getCodeValue() {
            return codeValue;
        }
    }

    /**
     * テスト用のエンティティ
     */
    public static class Entity extends BaseEntity {

        public Entity(Map<String, Object> params) {
            super(params);
        }

        @ValidateFor("pattern1")
        public static void validatePattern1(ValidationContext<Entity> context) throws Exception {
            ValidationUtil.validate(context, new String[] {"codeValue"});

            // コード値精査
            CodeValidationUtil.validate(context, "C00001", "PATTERN01", "codeValue");
        }

        @ValidateFor("pattern2")
        public static void validatePattern2(ValidationContext<Entity> context) throws Exception {
            ValidationUtil.validate(context, new String[] {"codeValue"});

            // コード値精査
            CodeValidationUtil.validate(context, "C00001", "PATTERN02", "codeValue");
        }

        @ValidateFor("pattern3")
        public static void validatePattern3(ValidationContext<Entity> context) throws Exception {
            ValidationUtil.validate(context, new String[] {"codeValue"});

            // コード値精査
            CodeValidationUtil.validate(context, "C00001", "PATTERN03", "codeValue", "message_id");
        }
    }

    public static class MockStringResourceHolder extends StringResourceHolder {
        @Override
        public StringResource get(final String messageId) throws MessageNotFoundException {
            return new StringResource() {
                @Override
                public String getId() {
                    return messageId;
                }

                @Override
                public String getValue(Locale locale) {
                    return messageId + ":メッセージ";
                }
            };
        }
    }
}

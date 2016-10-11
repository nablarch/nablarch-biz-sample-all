package please.change.me.core.validation.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import oracle.jdbc.pool.OracleDataSource;

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


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * {@link CodeValidationUtil}のテストクラス
 *
 * @author hisaaki sioiri
 */
public class CodeValidationUtilTest {

    /** テスト用のデータをセットアップするためのコネクション */
    private static Connection connection;

    @BeforeClass
    public static void setupClass() throws Exception {
        ResourceBundle rb = ResourceBundle.getBundle("db-config");
        OracleDataSource ds = new OracleDataSource();
        ds.setURL(rb.getString("db.url"));
        ds.setUser(rb.getString("db.user"));
        ds.setPassword(rb.getString("db.password"));

        connection = ds.getConnection();
        dropTable("CODE_NAME_TEST");
        dropTable("CODE_PATTERN_TEST");
        createTable();

        setupTestData();

        ComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "please/change/me/core/validation/code-validation-util-test.xml");
        SystemRepository.load(new DiContainer(loader));
        ThreadContext.setLanguage(Locale.JAPANESE);
    }

    /**
     * 指定されたテーブルを削除する。
     *
     * 削除時に発生した例外は無視する。
     *
     * @param tableName 削除対象のテーブル物理名
     */
    private static void dropTable(String tableName) throws Exception{
        Statement statement = connection.createStatement();
        try {
            statement.execute(String.format("DROP TABLE %s CASCADE CONSTRAINTS", tableName));
        } catch (SQLException ignored) {
            // nop
        }
        SystemRepository.clear();
        ThreadContext.clear();
    }

    /**
     * テストで使用するコードテーブルを作成する。
     */
    private static void createTable() throws Exception {
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE CODE_NAME_TEST ("
                + "  ID CHAR(6) NOT NULL ENABLE, "
                + "  VALUE NVARCHAR2(2) NOT NULL ENABLE, "
                + "  LANG CHAR(2) NOT NULL ENABLE, "
                + "  SORT_ORDER NUMBER(2,0) NOT NULL ENABLE, "
                + "  NAME NVARCHAR2(50) NOT NULL ENABLE, "
                + "  SHORT_NAME NVARCHAR2(50), "
                + "  OPTION01 NVARCHAR2(50), "
                + "  OPTION02 NVARCHAR2(50), "
                + "  OPTION03 NVARCHAR2(50), "
                + "  PRIMARY KEY (ID, VALUE, LANG)"
                + ")");

        statement.execute("CREATE TABLE CODE_PATTERN_TEST ("
                + "  ID CHAR(6) NOT NULL ENABLE, "
                + "  VALUE NVARCHAR2(2) NOT NULL ENABLE, "
                + "  PATTERN01 CHAR(1) NOT NULL ENABLE, "
                + "  PATTERN02 CHAR(1), "
                + "  PATTERN03 CHAR(1), "
                + "  PRIMARY KEY (ID, VALUE)"
                + ")");
        statement.close();
    }

    /**
     * テストで使用するデータをセットアップする。
     */
    private static void setupTestData() throws Exception {
        PreparedStatement codeName = connection.prepareStatement(
                "insert into code_name_test "
                + "(id,"
                + " value,"
                + " lang,"
                + " sort_order,"
                + " name)"
                + " values "
                + "(?, ?, ?, ?, ?)");
        codeName.setString(1, "C00001");
        codeName.setString(2, "01");
        codeName.setString(3, "ja");
        codeName.setInt(4, 1);
        codeName.setString(5, "コード１");
        codeName.addBatch();
        codeName.setString(2, "02");
        codeName.setInt(4, 2);
        codeName.setString(5, "コード２");
        codeName.addBatch();
        codeName.executeBatch();

        PreparedStatement codePattern = connection.prepareStatement(
                "insert into code_pattern_test"
                        + "(id,"
                        + "value,"
                        + "pattern01,"
                        + "pattern02,"
                        + "pattern03)"
                        + " values"
                        + "(?, ?, ?, ?, ?)");
        codePattern.setString(1, "C00001");
        codePattern.setString(2, "01");
        codePattern.setString(3, "1");
        codePattern.setString(4, "1");
        codePattern.setString(5, "0");
        codePattern.addBatch();
        codePattern.setString(2, "02");
        codePattern.setString(3, "1");
        codePattern.setString(4, "0");
        codePattern.setString(5, "0");
        codePattern.addBatch();
        codePattern.executeBatch();
        connection.commit();
    }


    /**
     * 後処理。
     *
     * データベースとの接続を切断する。
     * @throws Exception
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        if (connection != null) {
            connection.close();
        }
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

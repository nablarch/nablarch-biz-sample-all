package please.change.me.core.validation.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import please.change.me.core.message.MockStringResourceHolder;
import please.change.me.core.validation.convertor.TestTarget;
import please.change.me.core.validation.creator.ReflectionFormCreator;

import nablarch.core.ThreadContext;
import nablarch.core.validation.ValidationContext;
import nablarch.test.support.SystemRepositoryResource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * {@link please.change.me.core.validation.validator.JapaneseTelNumberValidator}のテストクラス
 *
 * @author Tomokazu Kagagwa
 */
public class JapaneseTelNumberValidatorTest {

    private JapaneseTelNumberValidator testee;
    private MockStringResourceHolder resource;
    private ValidationContext<TestTarget> context;

    private static final String[][] MESSAGES = {
                                               {"MSG00001", "ja", "{0}は電話番号ではありません。", "EN", "{0} isn't a telephone number."},
                                               {"MSG00002", "ja", "テストメッセージ01。", "EN", "test message 01."},
                                               {"PROP0001", "ja", "プロパティ1", "EN", "property1"},};

    @Rule
    public final SystemRepositoryResource repositoryResource = new SystemRepositoryResource(
            "please/change/me/core/validation/convertor-test-base.xml");

    @Before
    public void setUp() {
        resource = repositoryResource.getComponentByType(MockStringResourceHolder.class);
        resource.setMessages(MESSAGES);
        testee = new JapaneseTelNumberValidator();
        testee.setMessageId("MSG00001");
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        context = new ValidationContext<TestTarget>("", TestTarget.class, new ReflectionFormCreator(), params, "");
    }

    private JapaneseTelNumber required = new JapaneseTelNumber() {

        public Class<? extends Annotation> annotationType() {

            return JapaneseTelNumber.class;
        }

        public String messageId() {

            return "";
        }
    };

    @Test
    public void testValidateSuccessString() {

        assertTrue(testee.validate(context, "param", "PROP0001", required, "090-1111-2222"));
    }

    @Test
    public void testValidateFailString() {

        assertFalse(testee.validate(context, "param", "PROP0001", required, "-09011112222"));
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は電話番号ではありません。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateFailNotString() {

        try {
            assertFalse(testee.validate(context, "param", "PROP0001", required, 1));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("value isn't instance of String. property name = param, property message id  = PROP0001, property " +
                         "type = java.lang.Integer", e.getMessage());
        }
    }

    @Test
    public void testValidateFailNull() {

        assertTrue(testee.validate(context, "param", "PROP0001", required, null));
    }

    @Test
    public void testValidateFailWithAnnotationMessageId() {

        JapaneseTelNumber mailAddress = new JapaneseTelNumber() {

            public Class<? extends Annotation> annotationType() {

                return JapaneseTelNumber.class;
            }

            public String messageId() {

                return "MSG00002";
            }
        };

        assertFalse(testee.validate(context, "param", "PROP0001", mailAddress, "test.com"));
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("テストメッセージ01。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateWithParamArg() {
        Map<String, Object> params = new HashMap<String, Object>() {
            {
                put("messageId", "MSG00002");
            }
        };

        assertFalse(testee.validate(context, "param", "PROP0001", params, "test.com"));
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("テストメッセージ01。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateWithNoMessageIdParamArg() {
        Map<String, Object> params = new HashMap<String, Object>();

        assertFalse(testee.validate(context, "param", "PROP0001", params, "test.com"));
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は電話番号ではありません。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testGetAnnotationClass() {

        assertEquals(JapaneseTelNumber.class, testee.getAnnotationClass());
    }
}

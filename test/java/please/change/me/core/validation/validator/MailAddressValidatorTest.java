package please.change.me.core.validation.validator;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import nablarch.core.ThreadContext;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.validation.ValidationContext;
import nablarch.test.RepositoryInitializer;
import please.change.me.core.message.MockStringResourceHolder;
import please.change.me.core.validation.convertor.TestTarget;
import please.change.me.core.validation.creator.ReflectionFormCreator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link please.change.me.core.validation.validator.MailAddressValidator}のテストクラス。
 *
 * @author Tomokazu Kgawa
 */
public class MailAddressValidatorTest {

    private MailAddressValidator testee;
    private MockStringResourceHolder resource;
    private ValidationContext<TestTarget> context;

    private static final String[][] MESSAGES = {
                                               {"MSG00001", "ja", "{0}はメールアドレスではありません。", "EN", "{0} isn't a mail address."},
                                               {"MSG00002", "ja", "テストメッセージ01。", "EN", "test message 01."},
                                               {"PROP0001", "ja", "プロパティ1", "EN", "property1"},};

    @AfterClass
    public static void restoreRepository() {
        RepositoryInitializer.initializeDefaultRepository();
    }

    @Before
    public void setUp() {

        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader("please/change/me/core/validation/convertor-test-base.xml");
        DiContainer container = new DiContainer(loader);
        SystemRepository.load(container);

        resource = container.getComponentByType(MockStringResourceHolder.class);
        resource.setMessages(MESSAGES);
        testee = new MailAddressValidator();
        testee.setMessageId("MSG00001");
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        context = new ValidationContext<TestTarget>("", TestTarget.class, new ReflectionFormCreator(), params, "");
    }

    private MailAddress required = new MailAddress() {

        public Class<? extends Annotation> annotationType() {

            return MailAddress.class;
        }

        public String messageId() {

            return "";
        }
    };

    @Test
    public void testValidateSuccessString() {

        assertTrue(testee.validate(context, "param", "PROP0001", required, "12345@test.com"));
    }

    @Test
    public void testValidateFailString() {

        assertFalse(testee.validate(context, "param", "PROP0001", required, "test.com"));
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001はメールアドレスではありません。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateFailNotString() {

        assertFalse(testee.validate(context, "param", "PROP0001", required, 1));
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001はメールアドレスではありません。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateFailNullOrEmpty() {

        assertTrue(testee.validate(context, "param", "PROP0001", required, null));
        assertTrue(testee.validate(context, "param", "PROP0001", required, ""));
    }

    @Test
    public void testValidateFailWithAnnotationMessageId() {

        MailAddress mailAddress = new MailAddress() {
            public Class<? extends Annotation> annotationType() {

                return MailAddress.class;
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
        assertEquals("PROP0001はメールアドレスではありません。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testGetAnnotationClass() {

        assertEquals(MailAddress.class, testee.getAnnotationClass());
    }
}

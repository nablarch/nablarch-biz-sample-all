package please.change.me.form;

import nablarch.test.core.db.EntityTestSupport;
import nablarch.test.junit5.extension.db.EntityTest;
import org.junit.jupiter.api.Test;
import please.change.me.form.ProjectInsertMessageForm;

@EntityTest
class ProjectInsertMessageFormTest {

    private static final Class<?> targetClass = ProjectInsertMessageForm.class;

    private EntityTestSupport support;

    @Test
    public void 文字列長と文字種の単項目精査結果が正しいことを検証する() {
        String sheetName = "testCharsetAndLength";
        String id = "charsetAndLength";
        support.testValidateCharsetAndLength(targetClass, sheetName, id);
    }

    @Test
    public void 文字列長と文字種以外の単項目精査結果が正しいことを検証する() {
        String sheetName = "testSingleValidation";
        String id = "singleValidation";
        support.testSingleValidation(targetClass, sheetName, id);
    }

}
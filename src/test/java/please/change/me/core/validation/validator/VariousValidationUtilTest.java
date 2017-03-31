package please.change.me.core.validation.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * {@link please.change.me.core.validation.validator.VariousValidationUtil}のテストクラス
 *
 * @author Tomokazu Kagawa
 */
public class VariousValidationUtilTest {


    @Test
    public void testisValidMailAddress() {

        assertFalse("空文字", VariousValidationUtil.isValidMailAddress(""));
        assertFalse("null", VariousValidationUtil.isValidMailAddress(null));

        // ローカル部の長さに関する限界値分析
        // 64文字
        assertTrue("ローカル部64桁", VariousValidationUtil.isValidMailAddress("1234567891123456789212345678931234567894123456789512345678961234@test.com"));

        // 65文字
        assertFalse("ローカル部65桁", VariousValidationUtil.isValidMailAddress("12345678911234567892123456789312345678941234567895123456789612345@test.com"));

        // ドメイン部の長さに関する限界値分析
        // 255文字
        assertTrue("ドメイン部255桁",
                  VariousValidationUtil.isValidMailAddress("test@123456789.123456789212345678931234567894123456789512345678961234567897123456789812345678991234567890"
                                                           + "1234567891123456789212345678931234567894123456789512345678961234567897123456789812345678991234567890"
                                                           + "1234567891123456789212345678931234567894123456789512345"));

        // 256文字
        assertFalse("ドメイン部256桁",
                   VariousValidationUtil.isValidMailAddress("test@123456789.123456789212345678931234567894123456789512345678961234567897123456789812345678991234567890"
                                                            + "1234567891123456789212345678931234567894123456789512345678961234567897123456789812345678991234567890"
                                                            + "12345678911234567892123456789312345678941234567895123456"));

        // @が存在しない。
        assertFalse("@が存在しない", VariousValidationUtil.isValidMailAddress("test.com"));

        // @が先頭
        assertFalse("@が先頭", VariousValidationUtil.isValidMailAddress("@test.com"));

        // @が二つ以上存在する。
        assertFalse("@が二つ以上。", VariousValidationUtil.isValidMailAddress("test@test@co.com"));

        // 末尾に@が存在する。
        assertFalse("末尾に@", VariousValidationUtil.isValidMailAddress("test.com@"));

        // ドメイン部の先頭に"."が存在する。(ドメイン部に"."は2つ以上)
        assertFalse("ドメイン部の先頭に'.'", VariousValidationUtil.isValidMailAddress("test@.test.com"));

        // ドメイン部の先頭に"."が存在する。(ドメイン部に"."は1つ)
        assertFalse("ドメイン部の先頭に'.'", VariousValidationUtil.isValidMailAddress("test@.testcom"));

        // ドメイン部の末尾に"."が存在する。(ドメイン部に"."は1つ)
        assertFalse("ドメイン部の末尾に'.'", VariousValidationUtil.isValidMailAddress("test@testcom."));

        // ドメイン部の末尾に"."が存在する。(ドメイン部に"."は2つ以上。)
        assertFalse("ドメイン部の末尾に'.'", VariousValidationUtil.isValidMailAddress("test@test.com."));

        // ドメイン部にて'.'が連続する。(2つ)
        assertFalse("ドメイン部にて'.'が連続する。", VariousValidationUtil.isValidMailAddress("test@test..com"));

        // ドメイン部にて'.'が連続する。(3つ)
        assertFalse("ドメイン部にて'.'が連続する。", VariousValidationUtil.isValidMailAddress("test@test...com"));

        // ドメイン部にて'.'が連続する。(4つ)
        assertFalse("ドメイン部にて'.'が連続する。", VariousValidationUtil.isValidMailAddress("test@test....com"));

        // ローカル部にて'.'が連続する。(2つ)
        assertTrue("ローカル部にて'.'が連続する。", VariousValidationUtil.isValidMailAddress("tes..t@test.com"));

        // ローカル部にて'.'が連続する。(3つ)
        assertTrue("ローカル部にて'.'が連続する。", VariousValidationUtil.isValidMailAddress("tes...t@test.com"));

        // ローカル部にて'.'が連続する。(4つ)
        assertTrue("ローカル部にて'.'が連続する。", VariousValidationUtil.isValidMailAddress("tes....t@test.com"));

        // ローカル部の末尾に"."が存在する。
        assertTrue("ローカル部の末尾に'.'", VariousValidationUtil.isValidMailAddress("test.@test.com"));

        // ローカル部の先頭に"."が存在する。
        assertTrue("ローカル部の先頭に'.'", VariousValidationUtil.isValidMailAddress(".test@test.com"));

        // ローカル部にもドメイン部にも"."が存在しない。
        assertFalse("ローカル部にもドメイン部にも'.'が存在しない", VariousValidationUtil.isValidMailAddress("test@testcom"));

        // ローカル部には"."が存在するが、ドメイン部には存在しない。
        assertFalse("ローカル部には'.'が存在するが、ドメイン部には存在しない", VariousValidationUtil.isValidMailAddress("test.test@testcom"));

        // ローカル部には"."が存在しないが、ドメイン部には存在する。
        assertTrue("ローカル部には'.'が存在しないが、ドメイン部には存在する", VariousValidationUtil.isValidMailAddress("test@test.com"));

        // 文字種に関するチェック
        // 全て有効な文字
        assertTrue(VariousValidationUtil.isValidMailAddress("!#$%&'*+-/0123456789=?{|}~_`^@ABCDEFGHI.JKLMNOPQRSTUVWXYZ-abcdefghijklmnopqrstuvwxyz"));
        assertTrue(VariousValidationUtil.isValidMailAddress("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKL!#$%&*+-/=?^_`{|}~@1234567890tis.com"));
        char[] unavailableChars = {' ', '"', '(', ')', ',', ':', ';', '<', '>', '[', '\\', ']'};
        String invalidMailAddress;
        for (char unavailableChar : unavailableChars) {
            // 有効でない文字が先頭
            invalidMailAddress = unavailableChar + "test@test.com";
            assertFalse(invalidMailAddress, VariousValidationUtil.isValidMailAddress(invalidMailAddress));

            // 有効でない文字が真ん中
            invalidMailAddress = "test" + unavailableChar + "@test.com";
            assertFalse(invalidMailAddress, VariousValidationUtil.isValidMailAddress(invalidMailAddress));

            // 有効でない文字が末尾
            invalidMailAddress = "test@test.com" + unavailableChar;
            assertFalse(invalidMailAddress, VariousValidationUtil.isValidMailAddress(invalidMailAddress));
        }

        assertFalse("-が1つしかない。", VariousValidationUtil.isValidMailAddress("090-1234"));
        assertFalse("ドメイン部に不正な文字が含まれる。",  VariousValidationUtil.isValidMailAddress("invalid.domain@in'valid.com"));
        assertFalse("ドメイン部に不正な文字が含まれる。",  VariousValidationUtil.isValidMailAddress("invalid.domain@in_valid.com"));
        assertTrue("ドメイン部に使える文字",  VariousValidationUtil.isValidMailAddress("invalid.domain@in-valid.com"));
    }

    @Test
    public void testIsValidJapaneseTelNum() {

        // 空文字の場合
        assertTrue("空文字", VariousValidationUtil.isValidJapaneseTelNum(""));

        // nullの場合
        assertTrue("null", VariousValidationUtil.isValidJapaneseTelNum(null));

        // 文字種に関するチェック
        char[] invalidChars = {'１', '２', '３', '４', '５', '６', '７', '８', '９', '０', 'ー', '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',',
                              '.', '/', ':', ';', '<', '=', '>', '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
                              'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
                              'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~'};
        String invalidTelNum;
        for (char invalidChar : invalidChars) {
            // 有効でない文字が先頭にある。
            invalidTelNum = invalidChar + "9011112222";
            assertFalse(invalidTelNum, VariousValidationUtil.isValidJapaneseTelNum(invalidTelNum));
            // 有効でない文字が真ん中にある。
            invalidTelNum = "09011" + invalidChar + "12222";
            assertFalse(invalidTelNum, VariousValidationUtil.isValidJapaneseTelNum(invalidTelNum));
            // 有効でない文字が末尾にある。
            invalidTelNum = "0901111222" + invalidChar;
            assertFalse(invalidTelNum, VariousValidationUtil.isValidJapaneseTelNum(invalidTelNum));
        }

        // 有効な文字のみ（現行のすべての有効な電話番号パターンを網羅）
        assertFalse("9桁", VariousValidationUtil.isValidJapaneseTelNum("012345678"));
        for (int i = 1; i < 10; i++) {
            assertFalse("9桁(先頭が0以外)", VariousValidationUtil.isValidJapaneseTelNum(i + "12345678"));
        }
        assertTrue("10桁", VariousValidationUtil.isValidJapaneseTelNum("0123456789"));
        for (int i = 1; i < 10; i++) {
            assertFalse("10桁(先頭が0以外)", VariousValidationUtil.isValidJapaneseTelNum(i + "123456789"));
        }
        assertTrue("11桁", VariousValidationUtil.isValidJapaneseTelNum("01234567890"));
        for (int i = 1; i < 10; i++) {
            assertFalse("11桁(先頭が0以外)", VariousValidationUtil.isValidJapaneseTelNum(i + "1234567890"));
        }
        assertFalse("12桁", VariousValidationUtil.isValidJapaneseTelNum("012345678901"));
        for (int i = 1; i < 10; i++) {
            assertFalse("12桁(先頭が0以外)", VariousValidationUtil.isValidJapaneseTelNum(i + "12345678901"));
        }

        assertFalse("234型", VariousValidationUtil.isValidJapaneseTelNum("01-345-6789"));
        assertTrue("334型", VariousValidationUtil.isValidJapaneseTelNum("012-345-6789"));
        for (int i = 1; i < 10; i++) {
            assertFalse("334型(先頭が0以外)", VariousValidationUtil.isValidJapaneseTelNum(i + "12-345-6789"));
        }
        assertFalse("434型", VariousValidationUtil.isValidJapaneseTelNum("0123-345-6789"));
        assertFalse("324型", VariousValidationUtil.isValidJapaneseTelNum("012-34-6789"));
        assertFalse("354型", VariousValidationUtil.isValidJapaneseTelNum("012-34567-6789"));
        assertFalse("333型", VariousValidationUtil.isValidJapaneseTelNum("012-345-678"));
        assertFalse("335型", VariousValidationUtil.isValidJapaneseTelNum("012-345-67890"));

        assertFalse("144型", VariousValidationUtil.isValidJapaneseTelNum("1-2345-6789"));
        assertTrue("344型", VariousValidationUtil.isValidJapaneseTelNum("012-3456-7890"));
        for (int i = 1; i < 10; i++) {
            assertFalse("344型(先頭が0以外)", VariousValidationUtil.isValidJapaneseTelNum(i + "12-3456-7890"));
        }
        assertFalse("444型", VariousValidationUtil.isValidJapaneseTelNum("0123-4567-8901"));
        assertFalse("343型", VariousValidationUtil.isValidJapaneseTelNum("012-3456-890"));
        assertFalse("345型", VariousValidationUtil.isValidJapaneseTelNum("012-3456-78901"));

        assertFalse("324型", VariousValidationUtil.isValidJapaneseTelNum("012-34-5678"));
        assertTrue("424型", VariousValidationUtil.isValidJapaneseTelNum("0123-45-6789"));
        for (int i = 1; i < 10; i++) {
            assertFalse("424型(先頭が0以外)", VariousValidationUtil.isValidJapaneseTelNum(i + "123-45-6789"));
        }
        assertFalse("524型", VariousValidationUtil.isValidJapaneseTelNum("01234-56-7890"));
        assertFalse("414型", VariousValidationUtil.isValidJapaneseTelNum("0124-5-6789"));
        assertFalse("434型", VariousValidationUtil.isValidJapaneseTelNum("0123-456-7890"));
        assertFalse("423型", VariousValidationUtil.isValidJapaneseTelNum("0123-45-678"));
        assertFalse("425型", VariousValidationUtil.isValidJapaneseTelNum("0123-45-67890"));

        assertFalse("414型", VariousValidationUtil.isValidJapaneseTelNum("0123-5-6789"));
        assertTrue("514型", VariousValidationUtil.isValidJapaneseTelNum("01234-5-6789"));
        for (int i = 1; i < 10; i++) {
            assertFalse("514型(先頭が0以外)", VariousValidationUtil.isValidJapaneseTelNum(i + "1234-5-6789"));
        }
        assertFalse("614型", VariousValidationUtil.isValidJapaneseTelNum("012345-5-6789"));
        assertFalse("54型", VariousValidationUtil.isValidJapaneseTelNum("01234-5678"));
        assertFalse("534型", VariousValidationUtil.isValidJapaneseTelNum("01234-567-8901"));
        assertFalse("513型", VariousValidationUtil.isValidJapaneseTelNum("01234-5-678"));
        assertFalse("515型", VariousValidationUtil.isValidJapaneseTelNum("01234-5-67890"));

        assertFalse("144型", VariousValidationUtil.isValidJapaneseTelNum("0-2345-6789"));
        assertTrue("244型", VariousValidationUtil.isValidJapaneseTelNum("01-2345-6789"));
        for (int i = 1; i < 10; i++) {
            assertFalse("244型(先頭が0以外)", VariousValidationUtil.isValidJapaneseTelNum(i + "1-2345-6789"));
        }
        assertFalse("444型", VariousValidationUtil.isValidJapaneseTelNum("0123-5678-9012"));
        assertFalse("234型", VariousValidationUtil.isValidJapaneseTelNum("01-234-5678"));
        assertFalse("254型", VariousValidationUtil.isValidJapaneseTelNum("01-23456-7890"));
        assertFalse("243型", VariousValidationUtil.isValidJapaneseTelNum("01-2345-678"));
        assertFalse("245型", VariousValidationUtil.isValidJapaneseTelNum("01-2345-67890"));

    }

    @Test
    public void testIsValidJapaneseTelNum3Args() {

        // 空文字とnullの組み合わせ１
        assertTrue(VariousValidationUtil.isValidJapaneseTelNum(null, null, null));
        assertTrue(VariousValidationUtil.isValidJapaneseTelNum(null, null, ""));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum(null, null, "0000"));
        assertTrue(VariousValidationUtil.isValidJapaneseTelNum(null, "", null));
        assertTrue(VariousValidationUtil.isValidJapaneseTelNum(null, "", ""));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum(null, "", "0000"));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum(null, "1234", null));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum(null, "1234", ""));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum(null, "1234", "0000"));

        assertTrue(VariousValidationUtil.isValidJapaneseTelNum("", null, null));
        assertTrue(VariousValidationUtil.isValidJapaneseTelNum("", null, ""));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum("", null, "0000"));
        assertTrue(VariousValidationUtil.isValidJapaneseTelNum("", "", null));
        assertTrue(VariousValidationUtil.isValidJapaneseTelNum("", "", ""));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum("", "", "0000"));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum("", "1234", null));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum("", "1234", ""));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum("", "1234", "0000"));

        assertFalse(VariousValidationUtil.isValidJapaneseTelNum("0123", null, null));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum("0123", null, ""));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum("0123", null, "0000"));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum("0123", "", null));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum("0123", "", ""));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum("0123", "", "0000"));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum("0123", "1234", null));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum("0123", "1234", ""));
        assertFalse(VariousValidationUtil.isValidJapaneseTelNum("0123", "1234", "0000"));

        // パターン網羅
        assertFalse("234型", VariousValidationUtil.isValidJapaneseTelNum("01", "345", "6789"));
        assertTrue("334型", VariousValidationUtil.isValidJapaneseTelNum("012", "345", "6789"));
        for (int i = 1; i < 10; i++) {
            assertFalse("334型(先頭が0以外)", VariousValidationUtil.isValidJapaneseTelNum(i + "12", "345", "6789"));
        }
        assertFalse("434型", VariousValidationUtil.isValidJapaneseTelNum("0123", "345", "6789"));
        assertFalse("324型", VariousValidationUtil.isValidJapaneseTelNum("012", "34", "6789"));
        assertFalse("354型", VariousValidationUtil.isValidJapaneseTelNum("012", "34567", "6789"));
        assertFalse("333型", VariousValidationUtil.isValidJapaneseTelNum("012", "345", "678"));
        assertFalse("335型", VariousValidationUtil.isValidJapaneseTelNum("012", "345", "67890"));

        assertFalse("144型", VariousValidationUtil.isValidJapaneseTelNum("1", "2345", "6789"));
        assertTrue("344型", VariousValidationUtil.isValidJapaneseTelNum("012", "3456", "7890"));
        for (int i = 1; i < 10; i++) {
            assertFalse("344型(先頭が0以外)", VariousValidationUtil.isValidJapaneseTelNum(i + "12", "3456", "7890"));
        }
        assertFalse("444型", VariousValidationUtil.isValidJapaneseTelNum("0123", "4567", "8901"));
        assertFalse("343型", VariousValidationUtil.isValidJapaneseTelNum("012", "3456", "890"));
        assertFalse("345型", VariousValidationUtil.isValidJapaneseTelNum("012", "3456", "78901"));

        assertFalse("324型", VariousValidationUtil.isValidJapaneseTelNum("012", "34", "5678"));
        assertTrue("424型", VariousValidationUtil.isValidJapaneseTelNum("0123", "45", "6789"));
        for (int i = 1; i < 10; i++) {
            assertFalse("424型(先頭が0以外)", VariousValidationUtil.isValidJapaneseTelNum(i + "123", "45", "6789"));
        }
        assertFalse("524型", VariousValidationUtil.isValidJapaneseTelNum("01234", "56", "7890"));
        assertFalse("414型", VariousValidationUtil.isValidJapaneseTelNum("0124", "5", "6789"));
        assertFalse("434型", VariousValidationUtil.isValidJapaneseTelNum("0123", "456", "7890"));
        assertFalse("423型", VariousValidationUtil.isValidJapaneseTelNum("0123", "45", "678"));
        assertFalse("425型", VariousValidationUtil.isValidJapaneseTelNum("0123", "45", "67890"));

        assertFalse("414型", VariousValidationUtil.isValidJapaneseTelNum("0123", "5", "6789"));
        assertTrue("514型", VariousValidationUtil.isValidJapaneseTelNum("01234", "5", "6789"));
        for (int i = 1; i < 10; i++) {
            assertFalse("514型(先頭が0以外)", VariousValidationUtil.isValidJapaneseTelNum(i + "1234", "5", "6789"));
        }
        assertFalse("614型", VariousValidationUtil.isValidJapaneseTelNum("012345", "5", "6789"));
        assertFalse("534型", VariousValidationUtil.isValidJapaneseTelNum("01234", "567", "8901"));
        assertFalse("513型", VariousValidationUtil.isValidJapaneseTelNum("01234", "5", "678"));
        assertFalse("515型", VariousValidationUtil.isValidJapaneseTelNum("01234", "5", "67890"));

        assertFalse("144型", VariousValidationUtil.isValidJapaneseTelNum("0", "2345", "6789"));
        assertTrue("244型", VariousValidationUtil.isValidJapaneseTelNum("01", "2345", "6789"));
        for (int i = 1; i < 10; i++) {
            assertFalse("244型(先頭が0以外)", VariousValidationUtil.isValidJapaneseTelNum(i + "1", "2345", "6789"));
        }
        assertFalse("444型", VariousValidationUtil.isValidJapaneseTelNum("0123", "5678", "9012"));
        assertFalse("234型", VariousValidationUtil.isValidJapaneseTelNum("01", "234", "5678"));
        assertFalse("254型", VariousValidationUtil.isValidJapaneseTelNum("01", "23456", "7890"));
        assertFalse("243型", VariousValidationUtil.isValidJapaneseTelNum("01", "2345", "678"));
        assertFalse("245型", VariousValidationUtil.isValidJapaneseTelNum("01", "2345", "67890"));
    }
}

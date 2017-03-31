package please.change.me.common.mail.html;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.BodyPart;
import javax.mail.Multipart;

import nablarch.common.mail.MailAttachedFileTable.MailAttachedFile;

import org.junit.Test;

/**
 * HtmlMailContentCreatorの単体テストクラス。
 *
 * @author tani takanori
 *
 */
public class HtmlMailContentCreatorTest {


    /**
     * 添付ファイルなしのコンテンツ作成を確認する。
     * <br />
     * MimeMessageに設定後、{@link javax.mail.internet.MimeMessage#saveChanges() 変更を保存}した結果はHtmlMailSender側で実施する。
     * <br />
     * 本テストでは論理的な構造が構築されているかを確認する。
     *
     * @throws Exception 想定外の例外が発生した場合。
     */
    @Test
    public void testMakeMailContent() throws Exception {
        String mailBody = "<p>mailBody</p>";
        String charset = "utf-8";
        String alternativeText = "alternativeText";
        List<MailAttachedFile> empty = new ArrayList<MailAttachedFile>();
        Multipart actual = HtmlMailContentCreator.create(mailBody, charset, alternativeText, empty);
        assertThat("コンテンツはalternative", actual.getContentType(), containsString("multipart/alternative"));
        assertThat("TEXTとHTMLの二つがふくまれる", actual.getCount(), is(2));
        BodyPart first = actual.getBodyPart(0);
        assertThat("１つ目は代替テキスト", first.getDataHandler().getContentType(), containsString("text/plain"));
        assertThat("１つ目は代替テキスト", first.getContent(), instanceOf(String.class));
        assertThat("１つ目は代替テキスト", first.getContent().toString(), is(alternativeText));

        BodyPart second = actual.getBodyPart(1);

        assertThat("2つ目はHTML", second.getDataHandler().getContentType(), containsString("text/html"));
        assertThat("2つ目はHTML", second.getContent(), instanceOf(String.class));
        assertThat("2つ目はHTML", second.getContent().toString(), is(mailBody));
    }

    /**
     *
     * 添付ファイルが1つのコンテンツ作成を確認する。
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void testMakeMailContentWithOneAttachedFile() throws Exception {
        String mailBody = "mailBody";
        String charset = "utf-8";
        String alternativeText = "alternativeText";
        List<MailAttachedFile> attachedFiles = new ArrayList<MailAttachedFile>();
        attachedFiles.add(toMockData("1", "test.txt", "text/hoge", "これはテスト"));
        Multipart actual = HtmlMailContentCreator.create(mailBody, charset, alternativeText, attachedFiles);

        assertThat("コンテンツはmixed", actual.getContentType(), containsString("multipart/mixed"));
        assertThat("コンテンツの数は2つ(test part) + (file)", actual.getCount(), is(2));
        assertThat("コンテンツはMultiPart", actual.getBodyPart(0).getContent(), is(instanceOf(Multipart.class)));
        // ここからはテキスト部
        Multipart textPart = (Multipart) actual.getBodyPart(0).getContent();

        assertThat("コンテンツはalternative", textPart.getContentType(), containsString("multipart/alternative"));
        assertThat("TEXTとHTMLの二つがふくまれる", textPart.getCount(), is(2));
        BodyPart first = textPart.getBodyPart(0);
        assertThat("１つ目は代替テキスト", first.getDataHandler().getContentType(), containsString("text/plain"));
        assertThat("１つ目は代替テキスト", first.getContent(), instanceOf(String.class));
        assertThat("１つ目は代替テキスト", first.getContent().toString(), is(alternativeText));

        BodyPart second = textPart.getBodyPart(1);

        assertThat("2つ目はHTML", second.getDataHandler().getContentType(), containsString("text/html"));
        assertThat("2つ目はHTML", second.getContent(), instanceOf(String.class));
        assertThat("2つ目はHTML", second.getContent().toString(), is(mailBody));

        // ここからは添付ファイル部
        BodyPart filePart = actual.getBodyPart(1);
        assertThat("ファイルのコンテンツタイプ", filePart.getDataHandler().getContentType(), is("text/hoge"));
        assertThat("ファイルのコンテンツタイプ", filePart.getFileName(), is("test.txt"));
    }

    /**
    *
    * 添付ファイルが1つのコンテンツ作成を確認する。
    *
    * @throws Exception 想定外の例外が発生した場合
    */
   @Test
   public void testMakeMailContentWithManyAttachedFile() throws Exception {
       String mailBody = "mailBody";
       String charset = "utf-8";
       String alternativeText = "alternativeText";
       List<MailAttachedFile> attachedFiles = new ArrayList<MailAttachedFile>();
       attachedFiles.add(toMockData("1", "test.txt", "text/hoge", "これはテスト"));
       attachedFiles.add(toMockData("1", "nablarch.txt", "text/fuga", "これはテスト"));

       Multipart actual = HtmlMailContentCreator.create(mailBody, charset, alternativeText, attachedFiles);

       assertThat("コンテンツはmixed", actual.getContentType(), containsString("multipart/mixed"));
       assertThat("コンテンツの数は3つ(test part) + (file * 2)", actual.getCount(), is(3));
       assertThat("コンテンツはMultiPart", actual.getBodyPart(0).getContent(), is(instanceOf(Multipart.class)));
       // ここからはテキスト部
       Multipart textPart = (Multipart) actual.getBodyPart(0).getContent();

       assertThat("コンテンツはalternative", textPart.getContentType(), containsString("multipart/alternative"));
       assertThat("TEXTとHTMLの二つがふくまれる", textPart.getCount(), is(2));
       BodyPart first = textPart.getBodyPart(0);
       assertThat("１つ目は代替テキスト", first.getDataHandler().getContentType(), containsString("text/plain"));
       assertThat("１つ目は代替テキスト", first.getContent(), instanceOf(String.class));
       assertThat("１つ目は代替テキスト", first.getContent().toString(), is(alternativeText));

       BodyPart second = textPart.getBodyPart(1);

       assertThat("2つ目はHTML", second.getDataHandler().getContentType(), containsString("text/html"));
       assertThat("2つ目はHTML", second.getContent(), instanceOf(String.class));
       assertThat("2つ目はHTML", second.getContent().toString(), is(mailBody));

       // ここからは添付ファイル部
       BodyPart firstFilePart = actual.getBodyPart(1);
       assertThat("ファイルのコンテンツタイプ", firstFilePart.getDataHandler().getContentType(), is("text/hoge"));
       assertThat("ファイルのコンテンツタイプ", firstFilePart.getFileName(), is("test.txt"));

       BodyPart secondFilePart = actual.getBodyPart(2);
       assertThat("ファイルのコンテンツタイプ", secondFilePart.getDataHandler().getContentType(), is("text/fuga"));
       assertThat("ファイルのコンテンツタイプ", secondFilePart.getFileName(), is("nablarch.txt"));
   }

    /**
     * パラメータを持つモックに変換する。
     *
     * @param number シリアルナンバー
     * @param fileName ファイル名
     * @param contentType コンテンツタイプ
     * @param dummyData ﾀﾞﾐｰのデータ
     * @return モックの添付ファイル情報
     */
    private MailAttachedFile toMockData(String number, String fileName, String contentType, String dummyData) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("number", number);
        data.put("fileName", fileName);
        data.put("contentType", contentType);
        data.put("dummyData", dummyData);
        return new MockAttachedFile(data);
    }

    /**
     * Mock化した添付ファイルデータ
     *
     * @author tani takanori
     */
    private static class MockAttachedFile extends MailAttachedFile {
        private final Map<String, String> record;
        public MockAttachedFile(Map<String, String> record) {
            super(null);
            this.record = record;
        }

        @Override
        public int getSerialNumber() {
            return Integer.parseInt(record.get("number"));
        }

        @Override
        public String getFileName() {
            return record.get("fileName");
        }

        @Override
        public String getContextType() {
            return record.get("contentType");
        }

        @Override
        public byte[] getFile() {
            return record.get("dummyData").getBytes();
        }
    }
    
    /**
     * プライベートコンストラクタのカバレッジ対応。
     * 
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void testConstructor() throws Exception {
        Constructor<HtmlMailContentCreator> constructor = HtmlMailContentCreator.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}

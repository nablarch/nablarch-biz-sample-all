package com.example;

import nablarch.fw.web.HttpResponse;
import nablarch.test.core.http.RestTestSupport;
import nablarch.test.junit5.extension.http.RestTest;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

/**
 * {@link SampleAction}のテストクラス。
 *
 * @deprecated TODO 疎通確認用のクラスです。確認完了後、削除してください。
 */
@RestTest
class SampleApiTest {
    RestTestSupport support;

    /**
     * 正常終了のテストケース。
     * レスポンスがJSON
     */
    @Test
    void testFindJson() throws JSONException {
        String message = "ユーザー一覧取得（JSON）";
        HttpResponse response = support.sendRequest(support.get("/find/json"));
        support.assertStatusCode(message, HttpResponse.Status.OK, response);
        JSONAssert.assertEquals(message, "["
                        + "{\"userId\": 1,\"kanjiName\": \"名部楽太郎\",\"kanaName\": \"なぶらくたろう\"},"
                        + "{\"userId\": 2,\"kanjiName\": \"名部楽次郎\",\"kanaName\": \"なぶらくじろう\"}"
                        + "]"
                , response.getBodyString(), JSONCompareMode.LENIENT);
    }

    /**
     * 正常終了のテストケース。
     * レスポンスがXML
     */
    @Test
    void testFindXml() {
        String message = "ユーザー一覧取得（XML）";
        HttpResponse response = support.sendRequest(support.get("/find/xml"));
        support.assertStatusCode(message, HttpResponse.Status.OK, response);
        assertThat(response.getBodyString()
                , isSimilarTo("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                        + "<userList>"
                        + "    <sampleUser>"
                        + "        <kanaName>なぶらくたろう</kanaName>"
                        + "        <kanjiName>名部楽太郎</kanjiName>"
                        + "        <userId>1</userId>"
                        + "    </sampleUser>"
                        + "    <sampleUser>"
                        + "        <kanaName>なぶらくじろう</kanaName>"
                        + "        <kanjiName>名部楽次郎</kanjiName>"
                        + "        <userId>2</userId>"
                        + "    </sampleUser>"
                        + "</userList>")
                        .ignoreWhitespace());
    }
}

package please.change.me.simulator.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.fw.messaging.ReceivedMessage;
import nablarch.fw.messaging.ResponseMessage;
import nablarch.fw.messaging.SendingMessage;
import nablarch.fw.messaging.SyncMessage;
import nablarch.fw.web.HttpResponse;
import nablarch.test.RepositoryInitializer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * {@link MessageReadSupport}のテストクラス。
 *
 * @author T.Kawasaki
 * @since 1.4.2
 */
public class MessageReadSupportTest {

    /** テスト対象 */
    private MessageReadSupport messageReadSupport = new MessageReadSupport();


    @AfterClass
    public static void afterClass() {
    	RepositoryInitializer.initializeDefaultRepository();
    }

    @After
    public void tearDown() {
        // 読み出し位置を先頭に戻す。
        SendSyncSupportWrapperConcurrent.getInstance().reset();
    }

    /**
     * 正常系のテスト。
     * 1行だけテストデータが記述されたExcelについて、値を3回取得しても同じ値が返ってくる事を確認する。
     * (キャッシュが有効であることを確認するために繰り返し読みこむ)
     */
    @Test
    public void testGetSyncMessage() {
        DiContainer container = null;
        container = new DiContainer(new XmlComponentDefinitionLoader("outgoing-simulator-component-configuration.xml"));
        SystemRepository.load(container);
        
        container = new DiContainer(new XmlComponentDefinitionLoader("unit-test.xml"));
        SystemRepository.load(container);


        SyncMessage syncMessage = null;
        String contentType = null;
        //Excelから値を読み込めたか確認
        syncMessage = messageReadSupport.getSyncMessage("RM11AC0306");
        contentType = (String) syncMessage.getHeaderRecord().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));
        assertThat((String)syncMessage.getDataRecord().get("a"), is("あ"));
        assertThat((String)syncMessage.getDataRecord().get("b"), is("2"));

        //Excelから値を読み込めたか確認
        syncMessage = messageReadSupport.getSyncMessage("RM11AC0306");
        contentType = (String) syncMessage.getHeaderRecord().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));
        assertThat((String)syncMessage.getDataRecord().get("a"), is("あ"));
        assertThat((String)syncMessage.getDataRecord().get("b"), is("2"));

        //Excelから値を読み込めたか確認
        syncMessage = messageReadSupport.getSyncMessage("RM11AC0306");
        contentType = (String) syncMessage.getHeaderRecord().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));
        assertThat((String)syncMessage.getDataRecord().get("a"), is("あ"));
        assertThat((String)syncMessage.getDataRecord().get("b"), is("2"));
    }


    private static void loadConfig(String... configs) {
        for (String config : configs) {
            DiContainer container = new DiContainer(new XmlComponentDefinitionLoader(config));
            SystemRepository.load(container);
        }
    }

    /**
     * 正常系のテスト。
     * 2行だけテストデータが記述されたExcelについて、値を6回取得した際に、1行目、2行目、1行目、2行目、1行目、2行目の順で値が返ってくる事を確認する。
     * (キャッシュが有効であることを確認するために繰り返し読みこむ)
     */
    @Test
    public void testGetSyncMessageTwoRow() {
        DiContainer container = null;
        container = new DiContainer(new XmlComponentDefinitionLoader("outgoing-simulator-component-configuration.xml"));
        SystemRepository.load(container);
        
        container = new DiContainer(new XmlComponentDefinitionLoader("unit-test.xml"));
        SystemRepository.load(container);


        SyncMessage syncMessage = null;
        String contentType = null;
        //Excelから値を読み込めたか確認
        syncMessage = messageReadSupport.getSyncMessage("RM11AC0316");
        contentType = (String) syncMessage.getHeaderRecord().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));
        assertThat((String)syncMessage.getDataRecord().get("a"), is("あ"));
        assertThat((String)syncMessage.getDataRecord().get("b"), is("2"));

        //Excelから値を読み込めたか確認
        syncMessage = messageReadSupport.getSyncMessage("RM11AC0316");
        contentType = (String) syncMessage.getHeaderRecord().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));
        assertThat((String)syncMessage.getDataRecord().get("a"), is("い"));
        assertThat((String)syncMessage.getDataRecord().get("b"), is("3"));

        //Excelから値を読み込めたか確認
        syncMessage = messageReadSupport.getSyncMessage("RM11AC0316");
        contentType = (String) syncMessage.getHeaderRecord().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));
        assertThat((String)syncMessage.getDataRecord().get("a"), is("あ"));
        assertThat((String)syncMessage.getDataRecord().get("b"), is("2"));

        //Excelから値を読み込めたか確認
        syncMessage = messageReadSupport.getSyncMessage("RM11AC0316");
        contentType = (String) syncMessage.getHeaderRecord().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));
        assertThat((String)syncMessage.getDataRecord().get("a"), is("い"));
        assertThat((String)syncMessage.getDataRecord().get("b"), is("3"));

        //Excelから値を読み込めたか確認
        syncMessage = messageReadSupport.getSyncMessage("RM11AC0316");
        contentType = (String) syncMessage.getHeaderRecord().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));
        assertThat((String)syncMessage.getDataRecord().get("a"), is("あ"));
        assertThat((String)syncMessage.getDataRecord().get("b"), is("2"));

        //Excelから値を読み込めたか確認
        syncMessage = messageReadSupport.getSyncMessage("RM11AC0316");
        contentType = (String) syncMessage.getHeaderRecord().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));
        assertThat((String)syncMessage.getDataRecord().get("a"), is("い"));
        assertThat((String)syncMessage.getDataRecord().get("b"), is("3"));

    }

    /**
     * 正常系のテスト。
     * 1行だけ記述されている二つのExcelファイルを交互に読み込む。
     * (2回目の読み込みは、キャッシュからとなる)
     */
    @Test
    public void testGetSyncMessageMixReqId() {
        DiContainer container = null;
        container = new DiContainer(new XmlComponentDefinitionLoader("outgoing-simulator-component-configuration.xml"));
        SystemRepository.load(container);
        
        container = new DiContainer(new XmlComponentDefinitionLoader("unit-test.xml"));
        SystemRepository.load(container);


        SyncMessage syncMessage = null;
        String contentType = null;
        //Excelから値を読み込めたか確認
        syncMessage = messageReadSupport.getSyncMessage("RM11AC0326");
        contentType = (String) syncMessage.getHeaderRecord().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));
        assertThat((String)syncMessage.getDataRecord().get("a"), is("あ"));
        assertThat((String)syncMessage.getDataRecord().get("b"), is("2"));

        //Excelから値を読み込めたか確認
        syncMessage = messageReadSupport.getSyncMessage("RM11AC0336");
        contentType = (String) syncMessage.getHeaderRecord().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));
        assertThat((String)syncMessage.getDataRecord().get("a"), is("か"));
        assertThat((String)syncMessage.getDataRecord().get("b"), is("4"));

        //Excelから値を読み込めたか確認
        syncMessage = messageReadSupport.getSyncMessage("RM11AC0326");
        contentType = (String) syncMessage.getHeaderRecord().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));
        assertThat((String)syncMessage.getDataRecord().get("a"), is("あ"));
        assertThat((String)syncMessage.getDataRecord().get("b"), is("2"));

        //Excelから値を読み込めたか確認
        syncMessage = messageReadSupport.getSyncMessage("RM11AC0336");
        contentType = (String) syncMessage.getHeaderRecord().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));
        assertThat((String)syncMessage.getDataRecord().get("a"), is("か"));
        assertThat((String)syncMessage.getDataRecord().get("b"), is("4"));
    }

    /**
     * 正常系のテスト。
     * 1行だけテストデータが記述されたExcelについて、値を3回取得しても同じ値が返ってくる事を確認する。
     * (キャッシュが有効であることを確認するために繰り返し読みこむ)
     */
    @Test
    public void testGetSendingMessage() {
        DiContainer container = null;
        container = new DiContainer(new XmlComponentDefinitionLoader("outgoing-simulator-component-configuration.xml"));
        SystemRepository.load(container);
        
        container = new DiContainer(new XmlComponentDefinitionLoader("unit-test.xml"));
        SystemRepository.load(container);
         
        SendingMessage sendingMessage = null;
        //Excelから値を読み込めたか確認
        sendingMessage = messageReadSupport.getSendingMessage("RM11AC0305");
        assertThat((String)sendingMessage.getRecords().get(0).get("requestId"), is("12345"));
        assertThat((String)sendingMessage.getRecords().get(0).get("resendFlag"), is("0"));
        assertThat((String)sendingMessage.getRecords().get(0).get("statusCode"), is("200"));
        assertThat((String)sendingMessage.getRecords().get(0).get("userId"), is("2345"));
        assertThat((String)sendingMessage.getRecords().get(1).get("kanjiName"), is("あいうえお"));

        //Excelから値を読み込めたか確認
        sendingMessage = messageReadSupport.getSendingMessage("RM11AC0305");
        assertThat((String)sendingMessage.getRecords().get(0).get("requestId"), is("12345"));
        assertThat((String)sendingMessage.getRecords().get(0).get("resendFlag"), is("0"));
        assertThat((String)sendingMessage.getRecords().get(0).get("statusCode"), is("200"));
        assertThat((String)sendingMessage.getRecords().get(0).get("userId"), is("2345"));
        assertThat((String)sendingMessage.getRecords().get(1).get("kanjiName"), is("あいうえお"));

        //Excelから値を読み込めたか確認
        sendingMessage = messageReadSupport.getSendingMessage("RM11AC0305");
        assertThat((String)sendingMessage.getRecords().get(0).get("requestId"), is("12345"));
        assertThat((String)sendingMessage.getRecords().get(0).get("resendFlag"), is("0"));
        assertThat((String)sendingMessage.getRecords().get(0).get("statusCode"), is("200"));
        assertThat((String)sendingMessage.getRecords().get(0).get("userId"), is("2345"));
        assertThat((String)sendingMessage.getRecords().get(1).get("kanjiName"), is("あいうえお"));
    }

    /**
     * 正常系のテスト。
     * 2行だけテストデータが記述されたExcelについて、値を6回取得した際に、1行目、2行目、1行目、2行目、1行目、2行目の順で値が返ってくる事を確認する。
     * (キャッシュが有効であることを確認するために繰り返し読みこむ)
     */
    @Test
    public void testGetSendingMessageTwoRow() {
        DiContainer container = null;
        container = new DiContainer(new XmlComponentDefinitionLoader("outgoing-simulator-component-configuration.xml"));
        SystemRepository.load(container);
        
        container = new DiContainer(new XmlComponentDefinitionLoader("unit-test.xml"));
        SystemRepository.load(container);
         
        SendingMessage sendingMessage = null;
        //Excelから値を読み込めたか確認
        sendingMessage = messageReadSupport.getSendingMessage("RM11AC0315");
        assertThat((String)sendingMessage.getRecords().get(0).get("requestId"), is("12345"));
        assertThat((String)sendingMessage.getRecords().get(0).get("resendFlag"), is("0"));
        assertThat((String)sendingMessage.getRecords().get(0).get("statusCode"), is("200"));
        assertThat((String)sendingMessage.getRecords().get(0).get("userId"), is("2345"));
        assertThat((String)sendingMessage.getRecords().get(1).get("kanjiName"), is("あいうえお"));

        //Excelから値を読み込めたか確認
        sendingMessage = messageReadSupport.getSendingMessage("RM11AC0315");
        assertThat((String)sendingMessage.getRecords().get(0).get("requestId"), is("12345"));
        assertThat((String)sendingMessage.getRecords().get(0).get("resendFlag"), is("0"));
        assertThat((String)sendingMessage.getRecords().get(0).get("statusCode"), is("202"));
        assertThat((String)sendingMessage.getRecords().get(0).get("userId"), is("3456"));
        assertThat((String)sendingMessage.getRecords().get(1).get("kanjiName"), is("かきくけこ"));

        //Excelから値を読み込めたか確認
        sendingMessage = messageReadSupport.getSendingMessage("RM11AC0315");
        assertThat((String)sendingMessage.getRecords().get(0).get("requestId"), is("12345"));
        assertThat((String)sendingMessage.getRecords().get(0).get("resendFlag"), is("0"));
        assertThat((String)sendingMessage.getRecords().get(0).get("statusCode"), is("200"));
        assertThat((String)sendingMessage.getRecords().get(0).get("userId"), is("2345"));
        assertThat((String)sendingMessage.getRecords().get(1).get("kanjiName"), is("あいうえお"));

        //Excelから値を読み込めたか確認
        sendingMessage = messageReadSupport.getSendingMessage("RM11AC0315");
        assertThat((String)sendingMessage.getRecords().get(0).get("requestId"), is("12345"));
        assertThat((String)sendingMessage.getRecords().get(0).get("resendFlag"), is("0"));
        assertThat((String)sendingMessage.getRecords().get(0).get("statusCode"), is("202"));
        assertThat((String)sendingMessage.getRecords().get(0).get("userId"), is("3456"));
        assertThat((String)sendingMessage.getRecords().get(1).get("kanjiName"), is("かきくけこ"));

        //Excelから値を読み込めたか確認
        sendingMessage = messageReadSupport.getSendingMessage("RM11AC0315");
        assertThat((String)sendingMessage.getRecords().get(0).get("requestId"), is("12345"));
        assertThat((String)sendingMessage.getRecords().get(0).get("resendFlag"), is("0"));
        assertThat((String)sendingMessage.getRecords().get(0).get("statusCode"), is("200"));
        assertThat((String)sendingMessage.getRecords().get(0).get("userId"), is("2345"));
        assertThat((String)sendingMessage.getRecords().get(1).get("kanjiName"), is("あいうえお"));

        //Excelから値を読み込めたか確認
        sendingMessage = messageReadSupport.getSendingMessage("RM11AC0315");
        assertThat((String)sendingMessage.getRecords().get(0).get("requestId"), is("12345"));
        assertThat((String)sendingMessage.getRecords().get(0).get("resendFlag"), is("0"));
        assertThat((String)sendingMessage.getRecords().get(0).get("statusCode"), is("202"));
        assertThat((String)sendingMessage.getRecords().get(0).get("userId"), is("3456"));
        assertThat((String)sendingMessage.getRecords().get(1).get("kanjiName"), is("かきくけこ"));
    }

    /**
     * 正常系のテスト。
     * 1行だけ記述されている二つのExcelファイルを交互に読み込む。
     * (2回目の読み込みは、キャッシュからとなる)
     */
    @Test
    public void testGetSendingMessageMixReqId() {
        DiContainer container = null;
        container = new DiContainer(new XmlComponentDefinitionLoader("outgoing-simulator-component-configuration.xml"));
        SystemRepository.load(container);
        
        container = new DiContainer(new XmlComponentDefinitionLoader("unit-test.xml"));
        SystemRepository.load(container);
         
        SendingMessage sendingMessage = null;
        
        //Excelから値を読み込めたか確認
        sendingMessage = messageReadSupport.getSendingMessage("RM11AC0325");
        assertThat((String)sendingMessage.getRecords().get(0).get("requestId"), is("12345"));
        assertThat((String)sendingMessage.getRecords().get(0).get("resendFlag"), is("0"));
        assertThat((String)sendingMessage.getRecords().get(0).get("statusCode"), is("200"));
        assertThat((String)sendingMessage.getRecords().get(0).get("userId"), is("2345"));
        assertThat((String)sendingMessage.getRecords().get(1).get("kanjiName"), is("かきくけこ"));

        //Excelから値を読み込めたか確認
        sendingMessage = messageReadSupport.getSendingMessage("RM11AC0335");
        assertThat((String)sendingMessage.getRecords().get(0).get("requestId"), is("12345"));
        assertThat((String)sendingMessage.getRecords().get(0).get("resendFlag"), is("0"));
        assertThat((String)sendingMessage.getRecords().get(0).get("statusCode"), is("202"));
        assertThat((String)sendingMessage.getRecords().get(0).get("userId"), is("3456"));
        assertThat((String)sendingMessage.getRecords().get(1).get("kanjiName"), is("さしすせそ"));
        
        //Excelから値を読み込めたか確認
        sendingMessage = messageReadSupport.getSendingMessage("RM11AC0325");
        assertThat((String)sendingMessage.getRecords().get(0).get("requestId"), is("12345"));
        assertThat((String)sendingMessage.getRecords().get(0).get("resendFlag"), is("0"));
        assertThat((String)sendingMessage.getRecords().get(0).get("statusCode"), is("200"));
        assertThat((String)sendingMessage.getRecords().get(0).get("userId"), is("2345"));
        assertThat((String)sendingMessage.getRecords().get(1).get("kanjiName"), is("かきくけこ"));

        //Excelから値を読み込めたか確認
        sendingMessage = messageReadSupport.getSendingMessage("RM11AC0335");
        assertThat((String)sendingMessage.getRecords().get(0).get("requestId"), is("12345"));
        assertThat((String)sendingMessage.getRecords().get(0).get("resendFlag"), is("0"));
        assertThat((String)sendingMessage.getRecords().get(0).get("statusCode"), is("202"));
        assertThat((String)sendingMessage.getRecords().get(0).get("userId"), is("3456"));
        assertThat((String)sendingMessage.getRecords().get(1).get("kanjiName"), is("さしすせそ"));
    }
    
    /**
     * 正常系のテスト。
     * 1行だけテストデータが記述されたExcelについて、値を3回取得しても同じ値が返ってくる事を確認する。
     * (キャッシュが有効であることを確認するために繰り返し読みこむ)
     */
    @Test
    public void testGetResponseForHttp() {
        DiContainer container = null;
        container = new DiContainer(new XmlComponentDefinitionLoader("incoming-http-simulator-component-configuration.xml"));
        SystemRepository.load(container);
        
        container = new DiContainer(new XmlComponentDefinitionLoader("unit-test.xml"));
        SystemRepository.load(container);

        String contentType = null;
        HttpResponse responseForHttp = null;

        //Excelから値を読み込めたか確認
        responseForHttp = messageReadSupport.getResponseForHttp("RM11AC0303");
        assertThat(responseForHttp.getStatusCode(), is(202));
        assertThat(responseForHttp.getBodyString(), is("<?xml version=\"1.0\"?><response><failureCode></failureCode><userInfoId>HTTPメッセージングのレスポンスのテスト</userInfoId><dataKbn>0</dataKbn></response>"));
        contentType = responseForHttp.getHeaderMap().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));

        //Excelから値を読み込めたか確認
        responseForHttp = messageReadSupport.getResponseForHttp("RM11AC0303");
        assertThat(responseForHttp.getStatusCode(), is(202));
        assertThat(responseForHttp.getBodyString(), is("<?xml version=\"1.0\"?><response><failureCode></failureCode><userInfoId>HTTPメッセージングのレスポンスのテスト</userInfoId><dataKbn>0</dataKbn></response>"));
        contentType = responseForHttp.getHeaderMap().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));

        //Excelから値を読み込めたか確認
        responseForHttp = messageReadSupport.getResponseForHttp("RM11AC0303");
        assertThat(responseForHttp.getStatusCode(), is(202));
        assertThat(responseForHttp.getBodyString(), is("<?xml version=\"1.0\"?><response><failureCode></failureCode><userInfoId>HTTPメッセージングのレスポンスのテスト</userInfoId><dataKbn>0</dataKbn></response>"));
        contentType = responseForHttp.getHeaderMap().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));
    }
    

    /**
     * 正常系のテスト。
     * ExcelファイルにHTTPステータスコードが記述されていない場合、200が設定されることを確認する。
     */
    @Test
    public void testGetResponseForHttpUnDefStatusCode() {
        DiContainer container = null;
        container = new DiContainer(new XmlComponentDefinitionLoader("incoming-http-simulator-component-configuration.xml"));
        SystemRepository.load(container);
        
        container = new DiContainer(new XmlComponentDefinitionLoader("unit-test.xml"));
        SystemRepository.load(container);

        HttpResponse responseForHttp = null;

        //Excelファイルにステータスコードを書かなかった場合、200が設定されることを確認
        responseForHttp = messageReadSupport.getResponseForHttp("RM11AC0307");
        assertThat(responseForHttp.getStatusCode(), is(200));
    }


    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        System.in.read();
        System.out.println("start!!");
        new MessageReadSupportTest().testMultiThreadMultiReq();
    }

    @Test
    public void testMultiThreadSingleReq() throws ExecutionException, InterruptedException, IOException {
        loadConfig("incoming-http-simulator-component-configuration.xml", "unit-test.xml");

        ExecutorService service = Executors.newFixedThreadPool(300);

        final int methodCallCount = 50000;
        List<Future<HttpResponse>> results = new ArrayList<Future<HttpResponse>>(methodCallCount);
        for (int i = 0; i < methodCallCount; i++) {
            results.add(service.submit(new Callable<HttpResponse>() {
                @Override
                public HttpResponse call() throws Exception {
                    return messageReadSupport.getResponseForHttp("RM11AC0313");
                }
            }));
        }
        service.shutdown();

        assertThat(results.size(), is(methodCallCount));

        FrequencyCounter<String> counter = new FrequencyCounter<String>();
        for (Future<HttpResponse> result : results) {
            HttpResponse httpResponse = result.get();
            String body = httpResponse.getBodyString();
            counter.count(body);
        }
        assertThat("メッセージは２種類",
                   counter.size(), is(2));
        for (Integer cnt : counter.values()) {
            assertThat("2種類のメッセージの数が半々であること。",
                       cnt, is(methodCallCount / 2));
        }

    }




    private ExecutorService service;

    @After
    public void shutdown() {
        if (service != null) {
            service.shutdownNow();
        }
    }

    @Test
    public void testMultiThreadMultiReq() throws ExecutionException, InterruptedException, IOException {
        loadConfig("incoming-http-simulator-component-configuration.xml", "unit-test.xml");

        service = Executors.newFixedThreadPool(10);
        //ExecutorService service = Executors.newSingleThreadExecutor();
        // 10000回に呼び出し回数を設定
        final int methodCallCount = 100;
        long start = System.currentTimeMillis();

        List<Callable<HttpResponse>> callables = new ArrayList<Callable<HttpResponse>>(methodCallCount);

        for (int i = 0; i < methodCallCount; i++) {
            final String reqId;
            switch (i % 2) {
                case 0:
                    reqId = "RM11AC0303";
                    break;
                case 1:
                    reqId = "RM11AC0323";
                    break;
                default:
                    throw new IllegalStateException();
            }
            callables.add(new Callable<HttpResponse>() {
                @Override
                public HttpResponse call() throws Exception {
                    return messageReadSupport.getResponseForHttp(reqId);
                }
            });
        }

        List<Future<HttpResponse>> results = service.invokeAll(callables);
        service.shutdown();
        assertThat(results.size(), is(methodCallCount));

        FrequencyCounter<String> counter = new FrequencyCounter<String>();
        for (Future<HttpResponse> result : results) {
            HttpResponse httpResponse = result.get();
            String body = httpResponse.getBodyString();
            counter.count(body);
        }
        long end = System.currentTimeMillis();
        System.out.println("time=" + (end - start));

        if (service.awaitTermination(5, TimeUnit.SECONDS)) {
            service.shutdownNow();
        }

        assertThat("メッセージは２種類.[" + counter + "]",
                   counter.size(), is(2));
        for (Integer cnt : counter.values()) {
            assertThat("2種類のメッセージの数が半々であること。",
                       cnt, is(methodCallCount / 2));
        }

    }


    /**
     * 正常系のテスト。
     * 2行だけテストデータが記述されたExcelについて、値を6回取得した際に、1行目、2行目、1行目、2行目、1行目、2行目の順で値が返ってくる事を確認する。
     * (キャッシュが有効であることを確認するために繰り返し読みこむ)
     */
    @Test
    public void testGetResponseForHttpTwoRow() {
        DiContainer container = null;
        container = new DiContainer(new XmlComponentDefinitionLoader("incoming-http-simulator-component-configuration.xml"));
        SystemRepository.load(container);
        
        container = new DiContainer(new XmlComponentDefinitionLoader("unit-test.xml"));
        SystemRepository.load(container);

        String contentType = null;
        HttpResponse responseForHttp = null;

        //Excelから値を読み込めたか確認
        responseForHttp = messageReadSupport.getResponseForHttp("RM11AC0313");
        assertThat(responseForHttp.getStatusCode(), is(202));
        assertThat(responseForHttp.getBodyString(), is("<?xml version=\"1.0\"?><response><failureCode></failureCode><userInfoId>HTTPメッセージングのレスポンスのテスト1</userInfoId><dataKbn>0</dataKbn></response>"));
        contentType = responseForHttp.getHeaderMap().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));

        //Excelから値を読み込めたか確認
        responseForHttp = messageReadSupport.getResponseForHttp("RM11AC0313");
        assertThat(responseForHttp.getStatusCode(), is(400));
        assertThat(responseForHttp.getBodyString(), is("<?xml version=\"1.0\"?><response><failureCode></failureCode><userInfoId>HTTPメッセージングのレスポンスのテスト2</userInfoId><dataKbn>0</dataKbn></response>"));
        contentType = responseForHttp.getHeaderMap().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));

        //Excelから値を読み込めたか確認
        responseForHttp = messageReadSupport.getResponseForHttp("RM11AC0313");
        assertThat(responseForHttp.getStatusCode(), is(202));
        assertThat(responseForHttp.getBodyString(), is("<?xml version=\"1.0\"?><response><failureCode></failureCode><userInfoId>HTTPメッセージングのレスポンスのテスト1</userInfoId><dataKbn>0</dataKbn></response>"));
        contentType = responseForHttp.getHeaderMap().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));

        //Excelから値を読み込めたか確認
        responseForHttp = messageReadSupport.getResponseForHttp("RM11AC0313");
        assertThat(responseForHttp.getStatusCode(), is(400));
        assertThat(responseForHttp.getBodyString(), is("<?xml version=\"1.0\"?><response><failureCode></failureCode><userInfoId>HTTPメッセージングのレスポンスのテスト2</userInfoId><dataKbn>0</dataKbn></response>"));
        contentType = responseForHttp.getHeaderMap().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));

        //Excelから値を読み込めたか確認
        responseForHttp = messageReadSupport.getResponseForHttp("RM11AC0313");
        assertThat(responseForHttp.getStatusCode(), is(202));
        assertThat(responseForHttp.getBodyString(), is("<?xml version=\"1.0\"?><response><failureCode></failureCode><userInfoId>HTTPメッセージングのレスポンスのテスト1</userInfoId><dataKbn>0</dataKbn></response>"));
        contentType = responseForHttp.getHeaderMap().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));

        //Excelから値を読み込めたか確認
        responseForHttp = messageReadSupport.getResponseForHttp("RM11AC0313");
        assertThat(responseForHttp.getStatusCode(), is(400));
        assertThat(responseForHttp.getBodyString(), is("<?xml version=\"1.0\"?><response><failureCode></failureCode><userInfoId>HTTPメッセージングのレスポンスのテスト2</userInfoId><dataKbn>0</dataKbn></response>"));
        contentType = responseForHttp.getHeaderMap().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));
    }

    /**
     * 正常系のテスト。
     * 1行だけテストデータが記述されたExcelについて、値を3回取得しても同じ値が返ってくる事を確認する。
     * (キャッシュが有効であることを確認するために繰り返し読みこむ)
     */
    @Test
    public void testGetResponseForHttpMixReqId() {
        DiContainer container = null;
        container = new DiContainer(new XmlComponentDefinitionLoader("incoming-http-simulator-component-configuration.xml"));
        SystemRepository.load(container);
        
        container = new DiContainer(new XmlComponentDefinitionLoader("unit-test.xml"));
        SystemRepository.load(container);

        String contentType = null;
        //MessageReadSupport messageReadSupport = new MessageReadSupport();
        HttpResponse responseForHttp = null;

        //Excelから値を読み込めたか確認
        responseForHttp = messageReadSupport.getResponseForHttp("RM11AC0323");
        assertThat(responseForHttp.getStatusCode(), is(202));
        assertThat(responseForHttp.getBodyString(), is("<?xml version=\"1.0\"?><response><failureCode></failureCode><userInfoId>HTTPメッセージングのレスポンスのテストMIX1</userInfoId><dataKbn>0</dataKbn></response>"));
        contentType = responseForHttp.getHeaderMap().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));

        //Excelから値を読み込めたか確認
        responseForHttp = messageReadSupport.getResponseForHttp("RM11AC0333");
        assertThat(responseForHttp.getStatusCode(), is(202));
        assertThat(responseForHttp.getBodyString(), is("<?xml version=\"1.0\"?><response><failureCode></failureCode><userInfoId>HTTPメッセージングのレスポンスのテストMIX2</userInfoId><dataKbn>0</dataKbn></response>"));
        contentType = responseForHttp.getHeaderMap().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));

        //Excelから値を読み込めたか確認
        responseForHttp = messageReadSupport.getResponseForHttp("RM11AC0323");
        assertThat(responseForHttp.getStatusCode(), is(202));
        assertThat(responseForHttp.getBodyString(), is("<?xml version=\"1.0\"?><response><failureCode></failureCode><userInfoId>HTTPメッセージングのレスポンスのテストMIX1</userInfoId><dataKbn>0</dataKbn></response>"));
        contentType = responseForHttp.getHeaderMap().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));

        //Excelから値を読み込めたか確認
        responseForHttp = messageReadSupport.getResponseForHttp("RM11AC0333");
        assertThat(responseForHttp.getStatusCode(), is(202));
        assertThat(responseForHttp.getBodyString(), is("<?xml version=\"1.0\"?><response><failureCode></failureCode><userInfoId>HTTPメッセージングのレスポンスのテストMIX2</userInfoId><dataKbn>0</dataKbn></response>"));
        contentType = responseForHttp.getHeaderMap().get("Content-Type");
        assertThat(contentType, is("application/xml;charset=Shift-JIS"));
    }
    
    /**
     * 正常系のテスト。
     * 1行だけテストデータが記述されたExcelについて、値を3回取得しても同じ値が返ってくる事を確認する。
     * (キャッシュが有効であることを確認するために繰り返し読みこむ)
     * @throws Exception 例外
     */
    @Test
    public void testGetMessageForMom() throws Exception {
        DiContainer container = null;
        container = new DiContainer(new XmlComponentDefinitionLoader("incoming-mom-simulator-component-configuration.xml"));
        SystemRepository.load(container);
        
        container = new DiContainer(new XmlComponentDefinitionLoader("unit-test.xml"));
        SystemRepository.load(container);


        StringBuilder expectString = new StringBuilder();
        expectString.append("RM11AC0201           200                          \r\n");
        expectString.append("0あ　　　　0                   200                           \r\n");
        
        //ダミーの受信メッセージ
        ReceivedMessage receivedMessage = new ReceivedMessage(new byte[0]);
        receivedMessage.setMessageId("ID");
        receivedMessage.setReplyTo("RESPONSE");
        
        ResponseMessage responseMessage = null;
        String actualString = null;

        //Excelから値を読み込めたか確認
        responseMessage = messageReadSupport.getMessageForMom("RM11AC0304", receivedMessage);
        actualString = new String(responseMessage.getBodyBytes(), "MS932");
        assertThat(actualString, is(expectString.toString()));

        //Excelから値を読み込めたか確認
        responseMessage = messageReadSupport.getMessageForMom("RM11AC0304", receivedMessage);
        actualString = new String(responseMessage.getBodyBytes(), "MS932");
        assertThat(actualString, is(expectString.toString()));

        //Excelから値を読み込めたか確認
        responseMessage = messageReadSupport.getMessageForMom("RM11AC0304", receivedMessage);
        actualString = new String(responseMessage.getBodyBytes(), "MS932");
        assertThat(actualString, is(expectString.toString()));
    }
    
    /**
     * 正常系のテスト。
     * Excelファイルにフレームワーク制御ヘッダ部分が記載されていない場合。
     *
     * <p>1行だけテストデータが記述されたExcelについて、値を3回取得しても同じ値が返ってくる事を確認する。
     * (キャッシュが有効であることを確認するために繰り返し読みこむ)
     * @throws Exception 例外
     */
    @Test
    public void testGetMessageForMomLessHeader() throws Exception {
        DiContainer container = null;
        container = new DiContainer(new XmlComponentDefinitionLoader("incoming-mom-simulator-component-configuration.xml"));
        SystemRepository.load(container);
        
        container = new DiContainer(new XmlComponentDefinitionLoader("unit-test.xml"));
        SystemRepository.load(container);


        StringBuilder expectString = new StringBuilder();
        expectString.append("0あ　　　　0                   200                           \r\n");
        
        //ダミーの受信メッセージ
        ReceivedMessage receivedMessage = new ReceivedMessage(new byte[0]);
        receivedMessage.setMessageId("ID");
        receivedMessage.setReplyTo("RESPONSE");
        
        ResponseMessage responseMessage = null;
        String actualString = null;

        //Excelから値を読み込めたか確認
        responseMessage = messageReadSupport.getMessageForMom("RM11AC0308", receivedMessage);
        actualString = new String(responseMessage.getBodyBytes(), "MS932");
        assertThat(actualString, is(expectString.toString()));

        //Excelから値を読み込めたか確認
        responseMessage = messageReadSupport.getMessageForMom("RM11AC0308", receivedMessage);
        actualString = new String(responseMessage.getBodyBytes(), "MS932");
        assertThat(actualString, is(expectString.toString()));

        //Excelから値を読み込めたか確認
        responseMessage = messageReadSupport.getMessageForMom("RM11AC0308", receivedMessage);
        actualString = new String(responseMessage.getBodyBytes(), "MS932");
        assertThat(actualString, is(expectString.toString()));
    }

    /**
     * 正常系のテスト。
     * 2行だけテストデータが記述されたExcelについて、値を6回取得した際に、1行目、2行目、1行目、2行目、1行目、2行目の順で値が返ってくる事を確認する。
     * (キャッシュが有効であることを確認するために繰り返し読みこむ)
     * @throws Exception 例外
     */
    @Test
    public void testGetMessageForMomTwoRow() throws Exception {
        DiContainer container = null;
        container = new DiContainer(new XmlComponentDefinitionLoader("incoming-mom-simulator-component-configuration.xml"));
        SystemRepository.load(container);
        
        container = new DiContainer(new XmlComponentDefinitionLoader("unit-test.xml"));
        SystemRepository.load(container);


        StringBuilder expectString1 = new StringBuilder();
        expectString1.append("RM11AC0201           200                          \r\n");
        expectString1.append("0あ　　　　0                   200                           \r\n");
        
        StringBuilder expectString2 = new StringBuilder();
        expectString2.append("RM11AC0201           201                          \r\n");
        expectString2.append("0い　　　　0                   200                           \r\n");

        //ダミーの受信メッセージ
        ReceivedMessage receivedMessage = new ReceivedMessage(new byte[0]);
        receivedMessage.setMessageId("ID");
        receivedMessage.setReplyTo("RESPONSE");
        
        ResponseMessage responseMessage = null;
        String actualString = null;

        //Excelから値を読み込めたか確認
        responseMessage = messageReadSupport.getMessageForMom("RM11AC0314", receivedMessage);
        actualString = new String(responseMessage.getBodyBytes(), "MS932");
        assertThat(actualString, is(expectString1.toString()));

        //Excelから値を読み込めたか確認
        responseMessage = messageReadSupport.getMessageForMom("RM11AC0314", receivedMessage);
        actualString = new String(responseMessage.getBodyBytes(), "MS932");
        assertThat(actualString, is(expectString2.toString()));

        //Excelから値を読み込めたか確認
        responseMessage = messageReadSupport.getMessageForMom("RM11AC0314", receivedMessage);
        actualString = new String(responseMessage.getBodyBytes(), "MS932");
        assertThat(actualString, is(expectString1.toString()));

        //Excelから値を読み込めたか確認
        responseMessage = messageReadSupport.getMessageForMom("RM11AC0314", receivedMessage);
        actualString = new String(responseMessage.getBodyBytes(), "MS932");
        assertThat(actualString, is(expectString2.toString()));

        //Excelから値を読み込めたか確認
        responseMessage = messageReadSupport.getMessageForMom("RM11AC0314", receivedMessage);
        actualString = new String(responseMessage.getBodyBytes(), "MS932");
        assertThat(actualString, is(expectString1.toString()));

        //Excelから値を読み込めたか確認
        responseMessage = messageReadSupport.getMessageForMom("RM11AC0314", receivedMessage);
        actualString = new String(responseMessage.getBodyBytes(), "MS932");
        assertThat(actualString, is(expectString2.toString()));
    }
    
    /**
     * 正常系のテスト。
     * 1行だけテストデータが記述されたExcelについて、値を3回取得しても同じ値が返ってくる事を確認する。
     * (キャッシュが有効であることを確認するために繰り返し読みこむ)
     * @throws Exception 例外
     */
    @Test
    public void testGetMessageForMomMixReqId() throws Exception {
        DiContainer container = null;
        container = new DiContainer(new XmlComponentDefinitionLoader("incoming-mom-simulator-component-configuration.xml"));
        SystemRepository.load(container);
        
        container = new DiContainer(new XmlComponentDefinitionLoader("unit-test.xml"));
        SystemRepository.load(container);

        StringBuilder expectString1 = new StringBuilder();
        expectString1.append("RM11AC0201           201                          \r\n");
        expectString1.append("0い　　　　0                   200                           \r\n");

        StringBuilder expectString2 = new StringBuilder();
        expectString2.append("RM11AC0201           202                          \r\n");
        expectString2.append("0う　　　　0                   200                           \r\n");
        
        //ダミーの受信メッセージ
        ReceivedMessage receivedMessage = new ReceivedMessage(new byte[0]);
        receivedMessage.setMessageId("ID");
        receivedMessage.setReplyTo("RESPONSE");
        
        ResponseMessage responseMessage = null;
        String actualString = null;

        //Excelから値を読み込めたか確認
        responseMessage = messageReadSupport.getMessageForMom("RM11AC0324", receivedMessage);
        actualString = new String(responseMessage.getBodyBytes(), "MS932");
        assertThat(actualString, is(expectString1.toString()));

        //Excelから値を読み込めたか確認
        responseMessage = messageReadSupport.getMessageForMom("RM11AC0334", receivedMessage);
        actualString = new String(responseMessage.getBodyBytes(), "MS932");
        assertThat(actualString, is(expectString2.toString()));

        //Excelから値を読み込めたか確認
        responseMessage = messageReadSupport.getMessageForMom("RM11AC0324", receivedMessage);
        actualString = new String(responseMessage.getBodyBytes(), "MS932");
        assertThat(actualString, is(expectString1.toString()));

        //Excelから値を読み込めたか確認
        responseMessage = messageReadSupport.getMessageForMom("RM11AC0334", receivedMessage);
        actualString = new String(responseMessage.getBodyBytes(), "MS932");
        assertThat(actualString, is(expectString2.toString()));
    }
}

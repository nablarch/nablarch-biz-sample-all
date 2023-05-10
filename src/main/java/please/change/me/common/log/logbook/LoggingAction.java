package please.change.me.common.log.logbook;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.jaxrs.LogbookClientFilter;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.zalando.logbook.json.JsonPathBodyFilters.jsonPath;

@Path("/logging")
public class LoggingAction {

    /** ObjectMapper（未定義のプロパティは無視するように設定） */
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Content> get() throws Exception{
        // Logbookを生成（マスクしない場合）
        Logbook logbook = Logbook.builder().build();

        // Jersey ClientのFilterにLogbookを登録
        Client client = ClientBuilder.newClient()
                .register(new LogbookClientFilter(logbook));

        // Jersey Clientでリクエスト送信
        Response response = client.target("http://localhost:3000")
                .path("posts")
                .request()
                .get();

        // レスポンスをオブジェクトに変換
        String json = response.readEntity(String.class);
        return objectMapper.readValue(json, new TypeReference<List<Content>>() {});
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User post(UserInput userInput) throws Exception {
        // Logbookを生成（マスクする場合）
        Logbook logbook = Logbook.builder()
                .bodyFilter(jsonPath("$.id").replace("*****"))
                .build();

        // Jersey ClientのFilterにLogbookを登録
        Client client = ClientBuilder.newClient()
                .register(new LogbookClientFilter(logbook));

        // Jersey Clientでリクエスト送信
        Response response = client.target("http://localhost:3000")
                .path("users")
                .request()
                .post(Entity.entity(userInput, MediaType.APPLICATION_JSON_TYPE));

        // レスポンスをオブジェクトに変換
        String json = response.readEntity(String.class);
        return objectMapper.readValue(json, User.class);
    }
}

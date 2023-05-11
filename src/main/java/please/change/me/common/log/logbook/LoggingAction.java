package please.change.me.common.log.logbook;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.jaxrs.LogbookClientFilter;
import org.zalando.logbook.json.JsonPathBodyFilters;

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

@Path("/logbook")
public class LoggingAction {

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getAll() throws Exception{
        // Logbookを生成（デフォルト設定）
        Logbook logbook = Logbook.builder().build();

        // JAX-RSクライアントにLogbookを登録
        Client client = ClientBuilder.newClient()
                .register(new LogbookClientFilter(logbook));

        // JAX-RSクライアントでリクエスト送信
        Response response = client.target("http://localhost:3000")
                .path("/users")
                .request()
                .get();

        // レスポンスをオブジェクトに変換
        String json = response.readEntity(String.class);
        List<User> responseBody = new ObjectMapper().readValue(json, new TypeReference<>() {});

        return responseBody;
    }

    @GET
    @Path("/get/mask")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getMasked() throws Exception{
        // Logbookを生成（ボディにある配列内の id と username 項目をマスクする設定）
        Logbook logbook = Logbook.builder()
                .bodyFilter(JsonPathBodyFilters.jsonPath("$[*].id").replace("*****"))
                .bodyFilter(JsonPathBodyFilters.jsonPath("$[*].username").replace("*****"))
                .build();

        // JAX-RSクライアントにLogbookを登録
        Client client = ClientBuilder.newClient()
                .register(new LogbookClientFilter(logbook));

        // JAX-RSクライアントでリクエスト送信
        Response response = client.target("http://localhost:3000")
                .path("/users")
                .request()
                .get();

        // レスポンスをオブジェクトに変換
        String json = response.readEntity(String.class);
        List<User> responseBody = new ObjectMapper().readValue(json, new TypeReference<>() {});

        return responseBody;
    }

    @POST
    @Path("/post")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User post(User input) throws Exception {
        // Logbookを生成（デフォルト設定）
        Logbook logbook = Logbook.builder().build();

        // JAX-RSクライアントにLogbookを登録
        Client client = ClientBuilder.newClient()
                .register(new LogbookClientFilter(logbook));

        User requestBody = new User();
        requestBody.setId(input.getId());
        requestBody.setUsername(input.getUsername());

        // JAX-RSクライアントでリクエスト送信
        Response response = client.target("http://localhost:3000")
                .path("/users")
                .request()
                .post(Entity.json(requestBody));

        // レスポンスをオブジェクトに変換
        String json = response.readEntity(String.class);
        User responseBody = new ObjectMapper().readValue(json, User.class);

        return responseBody;
    }

    @POST
    @Path("/post/mask")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User postMasked(User input) throws Exception {
        // Logbookを生成（ボディの id 項目をマスクする設定）
        Logbook logbook = Logbook.builder()
                .bodyFilter(JsonPathBodyFilters.jsonPath("$.id").replace("*****"))
                .build();

        // JAX-RSクライアントにLogbookを登録
        Client client = ClientBuilder.newClient()
                .register(new LogbookClientFilter(logbook));

        User requestBody = new User();
        requestBody.setId(input.getId());
        requestBody.setUsername(input.getUsername());

        // JAX-RSクライアントでリクエスト送信
        Response response = client.target("http://localhost:3000")
                .path("/users")
                .request()
                .post(Entity.json(requestBody));

        // レスポンスをオブジェクトに変換
        String json = response.readEntity(String.class);
        User responseBody = new ObjectMapper().readValue(json, User.class);

        return responseBody;
    }
}

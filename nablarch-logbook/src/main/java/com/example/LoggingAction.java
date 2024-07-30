package com.example;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.jaxrs.LogbookClientFilter;
import org.zalando.logbook.json.JsonPathBodyFilters;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/logbook")
public class LoggingAction {

    /**
     * LogbookでGETリクエストのログを出力する。
     *
     * @return モックからのレスポンスデータ
     * @throws JacksonException JSON形式のデータ変換に失敗した場合
     */
    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getAll() throws JacksonException {
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
        List<User> responseBody = new ObjectMapper().readValue(json, new TypeReference<List<User>>() {});

        return responseBody;
    }

    /**
     * LogbookでGETリクエストのログをマスク処理した上で出力する。
     *
     * @return モックからのレスポンスデータ
     * @throws JacksonException JSON形式のデータ変換に失敗した場合
     */
    @GET
    @Path("/get/mask")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getMasked() throws JacksonException {
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
        List<User> responseBody = new ObjectMapper().readValue(json, new TypeReference<List<User>>() {});

        return responseBody;
    }

    /**
     * LogbookでPOSTリクエストのログを出力する。
     *
     * @return モックからのレスポンスデータ
     * @throws JacksonException JSON形式のデータ変換に失敗した場合
     */
    @POST
    @Path("/post")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User post(User input) throws JacksonException {
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

    /**
     * LogbookでPOSTリクエストのログをマスク処理した上で出力する。
     *
     * @return モックからのレスポンスデータ
     * @throws JacksonException JSON形式のデータ変換に失敗した場合
     */
    @POST
    @Path("/post/mask")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User postMasked(User input) throws JacksonException {
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

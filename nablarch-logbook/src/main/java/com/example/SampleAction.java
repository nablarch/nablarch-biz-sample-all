package com.example;

import com.example.dto.SampleUserListDto;
import com.example.entity.SampleUser;
import nablarch.common.dao.EntityList;
import nablarch.common.dao.UniversalDao;
import nablarch.fw.web.HttpRequest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * 疎通確認用のアクションクラス。
 *
 * @deprecated TODO 疎通確認用のクラスです。確認完了後、削除してください。
 */
@Path("/find")
public class SampleAction {

    /**
     * 検索処理。
     * <p>
     * 応答にJSONを使用する。
     * </p>
     *
     * @param req HTTPリクエスト
     * @return ユーザ情報(JSON)
     */
    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public EntityList<SampleUser> findProducesJson(HttpRequest req) {
        return UniversalDao.findAll(SampleUser.class);
    }

    /**
     * 検索処理。
     * <p>
     * 応答にXMLを使用する。
     * </p>
     *
     * @param req HTTPリクエスト
     * @return ユーザ情報(XML)
     */
    @GET
    @Path("/xml")
    @Produces(MediaType.APPLICATION_XML)
    public SampleUserListDto findProducesXml(HttpRequest req) {
        EntityList<SampleUser> sampleUserList = UniversalDao.findAll(SampleUser.class);
        return new SampleUserListDto(sampleUserList);
    }

}

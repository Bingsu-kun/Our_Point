package com.webproject.ourpoint.utils;

import com.webproject.ourpoint.model.marker.Category;
import com.webproject.ourpoint.model.marker.Marker;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Map;

public class ESUtils {

  private final RestHighLevelClient client;

  private IndexRequest request;

  private String INDEX_NAME = "marker";

  public ESUtils(RestHighLevelClient client) {
    this.client = client;
  }

  //-------------------------CRUD---------------------------------

  public void createDocument(Marker marker) throws IOException {
    request = new IndexRequest(INDEX_NAME).id(marker.getMarkerId().toString()).source(marker.toString(), XContentType.JSON);
    client.index(request, RequestOptions.DEFAULT);
  }

  public GetResponse getDocument(String markerId) throws IOException {
    GetRequest request = new GetRequest(INDEX_NAME, markerId);
    return client.get(request, RequestOptions.DEFAULT);
  }

  public void updateDocument(String markerId, Map<String , Object> bodyMap) throws IOException {
    UpdateRequest request = new UpdateRequest(INDEX_NAME, markerId).doc(bodyMap);
    client.update(request, RequestOptions.DEFAULT);
  }

  public void deleteDocument(String markerId) throws IOException {
    DeleteRequest request = new DeleteRequest(INDEX_NAME, markerId);
    client.delete(request, RequestOptions.DEFAULT);
  }

  //------------------------SEARCH------------------------------

  //전부 가져오기
  public SearchResponse searchAll() throws IOException {
    SearchRequest searchRequest = new SearchRequest(INDEX_NAME)
            .source(new SearchSourceBuilder().query(QueryBuilders.matchAllQuery()));

    return client.search(searchRequest, RequestOptions.DEFAULT);
  }

  //키워드와 카테고리에 따른 검색
  public SearchResponse search(String keyword, String category) throws IOException {
    SearchRequest searchRequest = new SearchRequest(INDEX_NAME)
            .source(new SearchSourceBuilder().query(
                    QueryBuilders.boolQuery()
                            .should(QueryBuilders.wildcardQuery("name", "*" + keyword + "*"))
                            .should(QueryBuilders.wildcardQuery("tags", "*" + keyword + "*"))
                            .should(QueryBuilders.wildcardQuery("description", "*" + keyword + "*"))
                            .should(QueryBuilders.wildcardQuery("category", "*" + category + "*"))
            ));

    return client.search(searchRequest, RequestOptions.DEFAULT);
  }

}

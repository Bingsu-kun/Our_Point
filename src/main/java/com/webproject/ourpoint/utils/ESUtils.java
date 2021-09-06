package com.webproject.ourpoint.utils;

import com.webproject.ourpoint.model.marker.Marker;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
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

  public void createDocument(Marker marker) {
    request = new IndexRequest(INDEX_NAME).id(marker.getMarkerId().toString()).source(marker.toString(), XContentType.JSON);
    try {
      client.index(request, RequestOptions.DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public GetResponse getDocument(String markerId) {
    GetRequest request = new GetRequest(INDEX_NAME, markerId);
    GetResponse getResponse = null;
    try {
      getResponse = client.get(request, RequestOptions.DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return getResponse;
  }

  public UpdateResponse updateDocument(String markerId, Map<String , Object> bodyMap){
    UpdateRequest request = new UpdateRequest(INDEX_NAME, markerId).doc(bodyMap);
    UpdateResponse updateResponse = null;
    try {
      updateResponse = client.update(request, RequestOptions.DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return updateResponse;
  }

  public DeleteResponse deleteDocument(String markerId) {
    DeleteRequest request = new DeleteRequest(INDEX_NAME, markerId);
    DeleteResponse deleteResponse = null;
    try {
      deleteResponse = client.delete(request, RequestOptions.DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return deleteResponse;
  }

  //------------------------SEARCH------------------------------

  //전부 가져오기
  public SearchResponse searchAll() {
    SearchRequest searchRequest = new SearchRequest(INDEX_NAME)
            .source(new SearchSourceBuilder().query(QueryBuilders.matchAllQuery()));
    SearchResponse searchResponse = null;
    try {
      searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return searchResponse;
  }

  //키워드와 카테고리에 따른 검색
  public SearchResponse search(String keyword, String category) {
    SearchRequest searchRequest = new SearchRequest(INDEX_NAME)
            .source(new SearchSourceBuilder().query(
                    QueryBuilders.boolQuery()
                            .should(QueryBuilders.wildcardQuery("name", "*" + keyword + "*"))
                            .should(QueryBuilders.wildcardQuery("tags", "*" + keyword + "*"))
                            .should(QueryBuilders.wildcardQuery("description", "*" + keyword + "*"))
                            .should(QueryBuilders.wildcardQuery("category", "*" + category + "*"))
            ));
    SearchResponse searchResponse = null;
    try {
      searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return searchResponse;
  }

}

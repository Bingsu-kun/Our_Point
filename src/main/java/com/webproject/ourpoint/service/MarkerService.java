package com.webproject.ourpoint.service;

import com.webproject.ourpoint.model.common.Id;
import com.webproject.ourpoint.model.marker.Category;
import com.webproject.ourpoint.model.marker.Marker;
import com.webproject.ourpoint.model.user.Fisher;
import com.webproject.ourpoint.utils.ESUtils;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class MarkerService {

  private final ESUtils esUtils;

  public MarkerService(ESUtils esUtils) {
    this.esUtils = esUtils;
  }

  //-----------------------------------------------------------------

  @Transactional
  public Marker createMarker(Id<Fisher,Long> fisherId, String name, String latitude, String longitude, Boolean isPrivate,
                           Category category, String tagString, String description) {

    checkArgument(fisherId != null, "fisherId must be provided.");
    checkArgument(latitude != null, "latitude must be provided.");
    checkArgument(longitude != null, "longitude must be provided.");
    checkArgument(category != null, "category must be provided.");

    Marker marker = new Marker(fisherId.value(),name,latitude,longitude,isPrivate,category,tagStringToTags(tagString),description);

    try {
      esUtils.createDocument(marker);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return marker;
  }

  @Transactional(readOnly = true)
  public Optional<String> getMarker(Long markerId) {
    GetResponse getResponse = null;

    try {
      getResponse = esUtils.getDocument(markerId.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }

    return Optional.ofNullable(getResponse.getSourceAsString());
  }

  @Transactional(readOnly = true)
  public Optional<SearchHit[]> getAllMarkers() {

    SearchResponse searchResponse = null;

    try {
      searchResponse = esUtils.searchAll();
    } catch (IOException e) {
      e.printStackTrace();
    }

    SearchHits searchHits = searchResponse.getHits();

    return Optional.ofNullable(searchHits.getHits());
  }

  @Transactional(readOnly = true)
  public Optional<SearchHit[]> searchByKeywordAndCategory(String keyword,Category category) {
    SearchResponse searchResponse = null;

    try {
      searchResponse = esUtils.search(keyword,category.name());
    } catch (Exception e) {
      e.printStackTrace();
    }

    SearchHits searchHits = searchResponse.getHits();

    return Optional.ofNullable(searchHits.getHits());
  }

  @Transactional
  @Modifying(clearAutomatically = true)
  public void updateMarker(Id<Fisher,Long> fisherId, Long markerId, Long mfId, String name, String latitude, String longitude, Boolean isPrivate, Category category, String tagString, String description) {

    checkArgument(Objects.equals(mfId,fisherId.value()),"you can't update this marker.");

    Map<String, Object> bodyMap = new HashMap<>();

    bodyMap.put("name",name);
    bodyMap.put("latitude",latitude);
    bodyMap.put("longitude",longitude);
    bodyMap.put("isPrivate",isPrivate);
    bodyMap.put("category",category.name());
    bodyMap.put("tags",tagStringToTags(tagString));
    bodyMap.put("description",description);

    try {
      esUtils.updateDocument(markerId.toString(), bodyMap);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Transactional
  public void deleteMarker(Id<Fisher, Long> fisherId, Long markerId, Long mfId) {

    checkArgument(Objects.equals(mfId, fisherId.value()),"you can't delete this marker.");

    try {
      esUtils.deleteDocument(markerId.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  //---------------------------util method-------------------------------

  public String[] tagStringToTags(String tagString) {
    String[] tags = tagString.split("#");
    checkArgument(tags.length <= 10, "tags must be lower than 10.");
    for(int i = 0; i < 10; i++) {
      tags[i] = tags[i].replaceAll(" ","");
    }
    return tags;
  }

}

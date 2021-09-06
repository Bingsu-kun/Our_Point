package com.webproject.ourpoint.service;

import com.webproject.ourpoint.errors.NotFoundException;
import com.webproject.ourpoint.model.common.Id;
import com.webproject.ourpoint.model.marker.Category;
import com.webproject.ourpoint.model.marker.Marker;
import com.webproject.ourpoint.model.user.Fisher;
import com.webproject.ourpoint.utils.ESUtils;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class MarkerService {

  private final ESUtils esUtils;

  private final FisherService fisherService;

  public MarkerService(ESUtils esUtils, FisherService fisherService) {
    this.esUtils = esUtils;
    this.fisherService = fisherService;
  }

  //-----------------------------------------------------------------
  //TODO-여기있는 트라이 캐치문 전부 ESUtil안에다 넣어주기
  @Transactional
  public Marker createMarker(Id<Fisher,Long> fisherId, String name, String latitude, String longitude, Boolean isPrivate,
                           Category category, String tagString, String description) {

    checkArgument(fisherId != null, "fisherId must be provided.");
    checkArgument(latitude != null, "latitude must be provided.");
    checkArgument(longitude != null, "longitude must be provided.");
    checkArgument(category != null, "category must be provided.");

    Marker marker = new Marker(fisherId.value(),name,latitude,longitude,isPrivate,category,tagStringToTags(tagString),description);
    esUtils.createDocument(marker);
    return marker;

  }

  @Transactional(readOnly = true)
  public Optional<String> getMarker(Long markerId) {
    return Optional.ofNullable(esUtils.getDocument(markerId.toString()).getSourceAsString());
  }

  @Transactional(readOnly = true)
  public Optional<Map<String,Object>> getMarkerToMap(Long markerId) {
    return Optional.ofNullable(esUtils.getDocument(markerId.toString()).getSourceAsMap());
  }

  @Transactional(readOnly = true)
  public Optional<SearchHit[]> getAllMarkers() {

    SearchResponse searchResponse = esUtils.searchAll();
    SearchHits searchHits = searchResponse.getHits();
    return Optional.ofNullable(searchHits.getHits());

  }

  @Transactional(readOnly = true)
  public Optional<SearchHit[]> searchByKeywordAndCategory(String keyword,Category category) {

    SearchResponse searchResponse = esUtils.search(keyword,category.name());
    SearchHits searchHits = searchResponse.getHits();
    return Optional.ofNullable(searchHits.getHits());

  }

  @Transactional
  @Modifying(clearAutomatically = true)
  public UpdateResponse updateMarker(Id<Fisher,Long> fisherId, Long markerId, Long mfId, String name, String latitude, String longitude,
                                     Boolean isPrivate, Category category, String tagString, String description) {

    checkArgument(Objects.equals(mfId,fisherId.value()),"you can't update this marker.");
    checkArgument(latitude != null, "latitude must be provided.");
    checkArgument(longitude != null, "longitude must be provided.");
    checkArgument(category != null, "category must be provided.");
    Map<String, Object> bodyMap = new HashMap<>();

    bodyMap.put("name",name);
    bodyMap.put("latitude",latitude);
    bodyMap.put("longitude",longitude);
    bodyMap.put("isPrivate",isPrivate);
    bodyMap.put("category",category.name());
    bodyMap.put("tags",tagStringToTags(tagString));
    bodyMap.put("description",description);

    return esUtils.updateDocument(markerId.toString(), bodyMap);

  }

  @Transactional
  public DeleteResponse deleteMarker(Id<Fisher, Long> fisherId, Long markerId, Long mfId) {
    checkArgument(Objects.equals(mfId, fisherId.value()),"you can't delete this marker.");
    return esUtils.deleteDocument(markerId.toString());
  }

  @Transactional
  public Integer like(Id<Fisher, Long> fisherId, Long markerId) {
    Map<String, Object> source = getMarkerToMap(markerId).orElseThrow(() -> new NotFoundException(Map.class, markerId));

    int likes = (int) source.get("likes");
    List<Long> likedFishers = (List<Long>) source.get("likedFishers");

    if (likedFishers.contains(fisherId.value())) {
      source.replace("likes", --likes);
      source.replace("likedFishers", likedFishers.remove(fisherId.value()));
      fisherService.removeLike(fisherId, markerId);
    } else {
      source.replace("likes", ++likes);
      source.replace("likedFishers", likedFishers.add(fisherId.value()));
      fisherService.addLike(fisherId, markerId);
    }

    return likes;

  }

  @Transactional(readOnly = true)
  public List<String> getLikedMarkers(Id<Fisher,Long> fisherId) {
    List<String> sources = new ArrayList<>();
    List<Long> markerIds = fisherService.getLikeList(fisherId);

    for (Long markerId : markerIds) {
      String markerSource = getMarker(markerId).orElseThrow(() -> new NotFoundException(String.class, markerId));
      if (markerSource == null)
        fisherService.removeLike(fisherId,markerId);
      else
        sources.add(markerSource);
    }
    return sources;

  }

  //---------------------------util method-------------------------------

  private String[] tagStringToTags(String tagString) {
    String[] tags = tagString.split("#");
    checkArgument(tags.length <= 10, "tags must be lower than 10.");
    for(int i = 0; i < 10; i++) {
      tags[i] = tags[i].replaceAll(" ","");
    }
    return tags;
  }

}

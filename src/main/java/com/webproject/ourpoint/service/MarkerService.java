package com.webproject.ourpoint.service;

import com.webproject.ourpoint.errors.NotFoundException;
import com.webproject.ourpoint.model.common.Id;
import com.webproject.ourpoint.model.liked.Liked;
import com.webproject.ourpoint.model.marker.Category;
import com.webproject.ourpoint.model.marker.Marker;
import com.webproject.ourpoint.model.user.Fisher;
import com.webproject.ourpoint.repository.LikedRepository;
import com.webproject.ourpoint.repository.MarkerRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class MarkerService {

  private final MarkerRepository markerRepository;
  private final LikedRepository likedRepository;

  public MarkerService(MarkerRepository markerRepository, LikedRepository likedRepository) {
    this.markerRepository = markerRepository;
    this.likedRepository = likedRepository;
  }

  //-----------------------------------------------------------------
  @Transactional
  public Marker createMarker(Id<Fisher,Long> fisherId, String name, String latitude, String longitude, Boolean isPrivate,
                           Category category, String tagString, String description) {

    checkArgument(fisherId != null, "fisherId must be provided.");
    checkArgument(latitude != null, "latitude must be provided.");
    checkArgument(longitude != null, "longitude must be provided.");
    checkArgument(category != null, "category must be provided.");

    Marker marker = new Marker(fisherId.value(),name,latitude,longitude,isPrivate,category.name(),trimTagString(tagString),description);
    return markerRepository.save(marker);
  }

  @Transactional
  @Modifying(clearAutomatically = true)
  public Marker updateMarker(Id<Fisher,Long> fisherId, Long markerId, Long mfId, String name, String latitude, String longitude,
                                     Boolean isPrivate, Category category, String tagString, String description) {

    checkArgument(Objects.equals(mfId,fisherId.value()),"you can't update this marker.");
    checkArgument(latitude != null, "latitude must be provided.");
    checkArgument(longitude != null, "longitude must be provided.");
    checkArgument(category != null, "category must be provided.");

    Marker marker = markerRepository.findById(markerId).orElseThrow(() -> new NotFoundException(Marker.class,markerId));
    marker.setLatlng(latitude,longitude);
    marker.setIsPrivate(isPrivate);
    marker.setCategory(category.name());
    marker.setName(name);
    marker.setTags(trimTagString(tagString));
    marker.setDescription(description);

    return markerRepository.save(marker);
  }
  @Transactional
  public void deleteMarker(Id<Fisher, Long> fisherId, Long markerId, Long mfId) {
    checkArgument(Objects.equals(mfId, fisherId.value()) || fisherId.value() == 1,"you can't delete this marker.");
    markerRepository.delete(markerRepository.getById(markerId));
  }

  @Transactional
  public Liked like(Id<Fisher, Long> fisherId, Long markerId) {
    Marker marker = markerRepository.findById(markerId).orElseThrow(() -> new NotFoundException(Marker.class, markerId));
    Liked liked = likedRepository.findLikedByIds(fisherId.value(),markerId);
    checkArgument(liked == null, "already liked.");
    return likedRepository.save(new Liked(fisherId.value(), markerId, marker.getFisherId()));
  }

  @Transactional
  public void dislike(Id<Fisher, Long> fisherId, Long markerId) {
    Liked liked = likedRepository.findLikedByIds(fisherId.value(),markerId);
    checkArgument(liked != null, "already disliked.");
    likedRepository.delete(liked);
  }

  @Transactional(readOnly = true)
  public int fisherLikeCount(Id<Fisher, Long> fisherId) {
    List<Liked> liked = likedRepository.findLikedByFisherId(fisherId.value());
    return liked.toArray().length;
  }

  @Transactional(readOnly = true)
  public List<Marker> myLikeList(Id<Fisher, Long> fisherId) {
    List<Liked> likeIds = likedRepository.findLikedByFisherId(fisherId.value());
    List<Marker> likeMarkers = new ArrayList<>();
    for (int i = 0; i < likeIds.toArray().length; i++) {
      Liked l = likeIds.get(i);
      likeMarkers.add(markerRepository.findById(l.getMarkerId()).orElseThrow(() -> new NotFoundException(String.valueOf(l.getMarkerId()), Marker.class)));
    }
    return likeMarkers;
  }

  @Transactional(readOnly = true)
  public int markerLikeCount(Long markerId) {
    List<Liked> liked = likedRepository.findLikedByMarkerId(markerId);
    return liked.toArray().length;
  }

  @Transactional(readOnly = true)
  public List<Integer> allMarkersLikeCount(Long[] markerIds) {
    List<Integer> likeCounts = new ArrayList<Integer>();
    for (Long id : markerIds) {
      likeCounts.add(markerLikeCount(id));
    }
    return likeCounts;
  }

  @Transactional(readOnly = true)
  public List<Marker> getAll() {
    return markerRepository.findAll();
  }

  //---------------------------util method-------------------------------

  private String[] tagStringTotagArray(String tagString) {
    String trimTag = trimTagString(tagString);
    String[] tags = trimTag.split("#");
    checkArgument(tags.length <= 10, "tags must be lower than 10.");
    return tags;
  }

  private String trimTagString(String tagString) {
    char[] tagSplit = tagString.replace(" ","").toCharArray();
    StringBuilder result = new StringBuilder();
    if (tagSplit[0] == '#') {
      for (int i = 1; i < tagSplit.length; i++) {
        result.append(tagSplit[i]);
      }
      return result.toString();
    }
    else
      return tagString.replace(" ","");
  }

}

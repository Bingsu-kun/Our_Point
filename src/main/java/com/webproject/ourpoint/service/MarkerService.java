package com.webproject.ourpoint.service;

import com.webproject.ourpoint.errors.NotFoundException;
import com.webproject.ourpoint.model.common.Id;
import com.webproject.ourpoint.model.common.Tag;
import com.webproject.ourpoint.model.liked.Liked;
import com.webproject.ourpoint.model.marker.Marker;
import com.webproject.ourpoint.model.user.Fisher;
import com.webproject.ourpoint.repository.LikedRepository;
import com.webproject.ourpoint.repository.MarkerRepository;
import com.webproject.ourpoint.repository.TagRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class MarkerService {

  private final MarkerRepository markerRepository;
  private final LikedRepository likedRepository;
  private final TagRepository tagRepository;

  public MarkerService(MarkerRepository markerRepository, LikedRepository likedRepository, TagRepository tagRepository) {
    this.markerRepository = markerRepository;
    this.likedRepository = likedRepository;
    this.tagRepository = tagRepository;
  }

  //-----------------------------------------------------------------
  @Transactional
  public Marker createMarker(Id<Fisher,Long> fisherId, String name, String latitude, String longitude, Boolean isPrivate,
                           String tagString, String description) {

    checkArgument(fisherId != null, "fisherId must be provided.");
    checkArgument(latitude != null, "latitude must be provided.");
    checkArgument(longitude != null, "longitude must be provided.");

    Marker marker = new Marker(fisherId.value(),name,latitude,longitude,isPrivate,trimTagString(tagString),description);

    addTags(tagString);

    return markerRepository.save(marker);
  }

  @Transactional
  @Modifying(clearAutomatically = true)
  public Marker updateMarker(Id<Fisher,Long> fisherId, Long markerId, Long mfId, String name, String latitude, String longitude,
                                     Boolean isPrivate, String tagString, String description) {

    checkArgument(Objects.equals(mfId,fisherId.value()),"you can't update this marker.");
    checkArgument(latitude != null, "latitude must be provided.");
    checkArgument(longitude != null, "longitude must be provided.");

    Marker marker = markerRepository.findById(markerId).orElseThrow(() -> new NotFoundException(Marker.class,markerId));
    marker.setLatlng(latitude,longitude);
    marker.setIsPrivate(isPrivate);
    marker.setName(name);
    if (!marker.getTags().equals(trimTagString(tagString))){
      marker.setTags(trimTagString(tagString));
      updateTags(marker.getTags(), trimTagString(tagString));
    }
    marker.setDescription(description);

    return markerRepository.save(marker);
  }

  @Transactional
  public void deleteMarker(Id<Fisher, Long> fisherId, Long markerId, Long mfId) {
    checkArgument(Objects.equals(mfId, fisherId.value()) || fisherId.value() == 1,"you can't delete this marker.");
    Marker currentMarker = markerRepository.getById(markerId);
    markerRepository.delete(currentMarker);
    decreaseTags(currentMarker.getTags());
    cascadeLikes(markerId);
  }

  @Transactional
  public List<Marker> myMarkers(Id<Fisher, Long> fisherId) {
    List<Marker> result = new ArrayList<Marker>();
    List<Marker> all = getAll();
    all.forEach((marker) -> {
      if (Objects.equals(marker.getFisherId(), fisherId.value())){
        result.add(marker);
      }
    });
    return result;
  }

  @Transactional(readOnly = true)
  public List<Marker> getAll() {
    return markerRepository.findAll();
  }

  //---------------------------util method-------------------------------

  private String trimTagString(String tagString) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < tagString.length(); i++) {
      if(String.valueOf(tagString.charAt(i)).matches("[a-zA-Z0-9 ㄱ-ㅎㅏ-ㅣ가-힣#]")) {
        result.append(tagString.charAt(i));
      }
      if(i+1 != tagString.length()){
        if(tagString.charAt(i+1) == '#'){
          result.append(" ");
        }
      }
    }
    return result.toString();
  }

  private void cascadeLikes(Long markerId) {
    List<Liked> likeds = likedRepository.findAll();
    likeds.forEach((element) -> {
      if (Objects.equals(element.getMarkerId(),markerId)) {
        likedRepository.delete(element);
      }
    });
  }

  private void addTags(String tagString) {
    for (String tag : trimTagString(tagString).replaceAll("#","").split(" ")) {
      Tag savedTag = tagRepository.findByTag(tag).orElse(new Tag(tag));
      savedTag.addUsed();
      tagRepository.save(savedTag);
    }
  }

  private void decreaseTags(String tagString) {
    for (String tag : trimTagString(tagString).replaceAll("#","").split(" ")) {
      Tag savedTag = tagRepository.findByTag(tag).orElseThrow(() -> new NotFoundException(Tag.class, tag));
      savedTag.decreaseUsed();
      if (savedTag.getUsed() == 0) {
        tagRepository.delete(savedTag);
      }
      else {
        tagRepository.save(savedTag);
      }
    }
  }

  private void updateTags(String beforeTag, String afterTag) {
    List<String> bTags = Arrays.asList(beforeTag.replaceAll("#", "").split(" "));
    List<String> aTags = Arrays.asList(afterTag.replaceAll("#", "").split(" "));

    for (String tag : bTags) {
      if (!afterTag.matches(tag)) {
        Tag savedTag = tagRepository.findByTag(tag).orElse(new Tag(tag));
        savedTag.addUsed();
        tagRepository.save(savedTag);
      }
    }

    for (String tag : aTags) {
      if (!beforeTag.matches(tag)) {
        Tag savedTag = tagRepository.findByTag(tag).orElseThrow(() -> new NotFoundException(Tag.class, tag));
        savedTag.decreaseUsed();
        if (savedTag.getUsed() == 0) {
          tagRepository.delete(savedTag);
        }
        else {
          tagRepository.save(savedTag);
        }
      }
    }
  }

}

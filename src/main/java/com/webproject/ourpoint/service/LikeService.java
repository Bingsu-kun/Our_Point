package com.webproject.ourpoint.service;

import com.webproject.ourpoint.errors.NotFoundException;
import com.webproject.ourpoint.model.common.Id;
import com.webproject.ourpoint.model.liked.Liked;
import com.webproject.ourpoint.model.marker.Marker;
import com.webproject.ourpoint.model.user.Fisher;
import com.webproject.ourpoint.repository.LikedRepository;
import com.webproject.ourpoint.repository.MarkerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class LikeService {

  private final LikedRepository likedRepository;
  private final MarkerRepository markerRepository;

  public LikeService(LikedRepository likedRepository, MarkerRepository markerRepository) {
    this.likedRepository = likedRepository;
    this.markerRepository = markerRepository;
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
  public List<Integer> allMarkersLikeCount(List<Long> markerIds) {
    List<Integer> likeCounts = new ArrayList<>();
    for (Long mk : markerIds) {
      likeCounts.add(markerLikeCount(mk));
    }
    return likeCounts;
  }

  @Transactional(readOnly = true)
  public List<Liked> getAll() {
    return likedRepository.findAll();
  }

}

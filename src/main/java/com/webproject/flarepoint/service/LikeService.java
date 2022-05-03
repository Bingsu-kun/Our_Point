package com.webproject.flarepoint.service;

import com.webproject.flarepoint.errors.NotFoundException;
import com.webproject.flarepoint.model.common.Id;
import com.webproject.flarepoint.model.liked.Liked;
import com.webproject.flarepoint.model.marker.Marker;
import com.webproject.flarepoint.model.user.User;
import com.webproject.flarepoint.repository.LikedRepository;
import com.webproject.flarepoint.repository.MarkerRepository;
import com.webproject.flarepoint.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class LikeService {

  private final UserRepository userRepository;
  private final LikedRepository likedRepository;
  private final MarkerRepository markerRepository;

  public LikeService(UserRepository userRepository, LikedRepository likedRepository, MarkerRepository markerRepository) {
    this.userRepository = userRepository;
    this.likedRepository = likedRepository;
    this.markerRepository = markerRepository;
  }

  @Transactional
  public Liked like(Id<User, Long> userId, Long markerId) {
    User user = userRepository.findById(userId.value()).orElseThrow(() -> new NotFoundException("user not exist."));
    Marker marker = markerRepository.findById(markerId).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."));
    Liked liked = likedRepository.findByUserAndMarker(user,marker);
    checkArgument(liked == null, "already liked.");
    return likedRepository.save(new Liked(user, marker, marker.getUser().getId()));
  }

  @Transactional
  public void dislike(Id<User, Long> userId, Long markerId) {
    Liked liked = likedRepository.findByUserAndMarker(userRepository.findById(userId.value()).orElseThrow(() -> new NotFoundException("user not exist.")) ,markerRepository.findById(markerId).orElseThrow(() -> new NotFoundException("찾을 수 없습니다.")));
    checkArgument(liked != null, "already disliked.");
    likedRepository.delete(liked);
  }

  @Transactional(readOnly = true)
  public int userLikeCount(Id<User, Long> userId) {
    List<Liked> liked = likedRepository.findByUser(userRepository.findById(userId.value()).orElseThrow(() -> new NotFoundException("user not exist.")));
    return liked.toArray().length;
  }

  @Transactional(readOnly = true)
  public List<Marker> myLikeList(Id<User, Long> userId) {
    List<Liked> likeIds = likedRepository.findByUser(userRepository.findById(userId.value()).orElseThrow(() -> new NotFoundException("user not exist.")));
    List<Marker> likeMarkers = new ArrayList<>();
    for (int i = 0; i < likeIds.toArray().length; i++) {
      Liked l = likeIds.get(i);
      likeMarkers.add(markerRepository.findById(l.getMarker().getMarkerId()).orElseThrow(() -> new NotFoundException("찾을 수 없습니다.")));
    }
    return likeMarkers;
  }

  @Transactional(readOnly = true)
  public int markerLikeCount(Long markerId) {
    List<Liked> liked = likedRepository.findByMarker(markerRepository.findById(markerId).orElseThrow(() -> new NotFoundException("찾을 수 없습니다.")));
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

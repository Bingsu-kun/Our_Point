package com.webproject.ourpoint.repository;

import com.webproject.ourpoint.model.liked.Liked;
import com.webproject.ourpoint.model.marker.Marker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikedRepository extends JpaRepository<Liked, Long> {

  @Query(value = "SELECT * FROM liked WHERE fisherId = :fisherId",nativeQuery = true)
  List<Liked> findLikedByFisherId(Long fisherId);

  @Query(value = "SELECT * FROM liked WHERE markerId = :markerId", nativeQuery = true)
  List<Liked> findLikedByMarkerId(Long markerId);

  @Query(value = "SELECT * FROM liked WHERE fisherId = :fisherId AND markerId = :markerId",nativeQuery = true)
  Liked findLikedByIds(Long fisherId, Long markerId);

}

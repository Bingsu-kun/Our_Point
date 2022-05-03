package com.webproject.flarepoint.repository;

import com.webproject.flarepoint.model.liked.Liked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikedRepository extends JpaRepository<Liked, Long> {

  List<Liked> findByUserId(Long userId);

  List<Liked> findByMarkerId(Long markerId);

  Liked findByUserIdAndMarkerId(Long userId, Long markerId);

}

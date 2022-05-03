package com.webproject.flarepoint.repository;

import com.webproject.flarepoint.model.liked.Liked;
import com.webproject.flarepoint.model.marker.Marker;
import com.webproject.flarepoint.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikedRepository extends JpaRepository<Liked, Long> {

  List<Liked> findByUser(User user);

  List<Liked> findByMarker(Marker marker);

  Liked findByUserAndMarker(User user, Marker marker);

}

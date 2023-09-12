package com.webproject.flarepoint.repository;

import com.webproject.flarepoint.model.marker.Marker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarkerRepository extends JpaRepository<Marker,Long> {
}

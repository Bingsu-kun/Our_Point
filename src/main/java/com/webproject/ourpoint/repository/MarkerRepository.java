package com.webproject.ourpoint.repository;

import com.webproject.ourpoint.model.marker.Marker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarkerRepository extends JpaRepository<Marker,Long> {
}

package com.webproject.flarepoint.repository;

import com.webproject.flarepoint.model.common.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

  Optional<Tag> findByTag(String tag);

  List<Tag> findAllByOrderByUsedDesc();

}

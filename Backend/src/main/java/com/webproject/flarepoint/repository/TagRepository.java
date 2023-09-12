package com.webproject.flarepoint.repository;

import com.webproject.flarepoint.model.common.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

  @Query(value = "select * from tag where tag = :tag",nativeQuery = true)
  Optional<Tag> findByTag(String tag);

  @Query(value = "select * from tag order by used desc",nativeQuery = true)
  List<Tag> topTags();

}

package com.webproject.ourpoint.model.marker;


import com.webproject.ourpoint.model.user.Fisher;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Builder
@EqualsAndHashCode
@Getter
@ToString
@NoArgsConstructor
public class Marker {

  private Long markerId;

  private Long fisherId;

  private String name;

  private String latitude;

  private String longitude;

  private Boolean isPrivate;

  private int likes;

  private List<Long> likedFishers;

  private String category;

  //tag들은 Service에서 #단위로 쪼개져서 String 배열로 저장
  private String[] tags;

  private String description;

  private LocalDateTime createdAt;

  public Marker(Long fisherId, String name, String latitude, String longitude, Boolean isPrivate,
                Category category, String[] tags, String description) {
    this(null,fisherId,name,latitude,longitude,isPrivate,0,null,category,tags,description,now());
  }

  public Marker(Long markerId, Long fisherId, String name, String latitude, String longitude, Boolean isPrivate,
                int likes, List<Long> likedFishers, Category category, String[] tags, String description, LocalDateTime createdAt) {
    checkArgument(fisherId != null, "fisherId must be provided.");
    checkArgument(latitude != null, "latitude must be provided.");
    checkArgument(longitude != null, "longitude must be provided.");
    checkArgument(isPrivate != null,"isPrivate must be provided.");
    checkArgument(category != null, "category must be provided.");
    checkArgument(tags.length <= 10, "tags must be less than 10");
    checkArgument(description.length() <= 100, "description must be lower than 100");

    this.markerId = markerId;
    this.fisherId = fisherId;
    this.name = name;
    this.latitude = latitude;
    this.longitude = longitude;
    this.isPrivate = isPrivate;
    this.likes = likes;
    this.likedFishers = likedFishers;
    this.category = category.name();
    this.tags = tags;
    this.description = description;
    this.createdAt = defaultIfNull(createdAt, now());
  }

}

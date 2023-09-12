package com.webproject.flarepoint.model.marker;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkArgument;
import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@EqualsAndHashCode
@Getter
@ToString
@NoArgsConstructor
@Entity(name = "marker")
public class Marker {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "marker_id_seq")
  @SequenceGenerator(name = "marker_id_seq", sequenceName = "marker_id_seq", allocationSize = 1)
  private Long markerId;

  @Column(nullable = false)
  private Long userId;

  private String name;

  @Column(nullable = false)
  private String latitude;

  @Column(nullable = false)
  private String longitude;

  @Column(nullable = false)
  private String place_addr;

  @Column(nullable = false)
  private Boolean isPrivate = false;

  private String tags;

  private String description;

  private LocalDateTime createdAt;

  public Marker(Long userId, String name, String latitude, String longitude, String place_addr, Boolean isPrivate, String tags, String description) {
    this(null, userId,name,latitude,longitude,place_addr,isPrivate,tags,description,now());
  }

  public Marker(Long markerId, Long userId, String name, String latitude, String longitude, String place_addr, Boolean isPrivate,
                String tags, String description, LocalDateTime createdAt) {
    checkArgument(userId != null, "userId must be provided.");
    checkArgument(latitude != null, "latitude must be provided.");
    checkArgument(longitude != null, "longitude must be provided.");
    checkArgument(isPrivate != null,"isPrivate must be provided.");
    checkArgument(description.length() <= 200, "description must be lower than 200");

    this.markerId = markerId;
    this.userId = userId;
    this.name = name;
    this.latitude = latitude;
    this.longitude = longitude;
    this.place_addr = place_addr;
    this.isPrivate = isPrivate;
    this.tags = tags;
    this.description = description;
    this.createdAt = defaultIfNull(createdAt, now());
  }

  public void setLatlng(String latitude, String longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public void setPlaceAddr(String place_addr) {
    this.place_addr = place_addr;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setIsPrivate(boolean isPrivate) {
    this.isPrivate = isPrivate;
  }

}

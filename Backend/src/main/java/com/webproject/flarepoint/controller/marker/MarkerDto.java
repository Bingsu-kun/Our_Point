package com.webproject.flarepoint.controller.marker;

import com.webproject.flarepoint.model.marker.Marker;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

import static org.springframework.beans.BeanUtils.copyProperties;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class MarkerDto {

  private Long markerId;

  private Long userId;

  private String name;

  private String latitude;

  private String longitude;

  private String place_addr;

  private Boolean isPrivate;

  private String tags;

  private String description;

  private LocalDateTime createdAt;

  public MarkerDto(Marker source) {
    copyProperties(source, this);
  }
}

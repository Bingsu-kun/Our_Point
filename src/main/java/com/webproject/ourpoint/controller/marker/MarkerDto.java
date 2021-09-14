package com.webproject.ourpoint.controller.marker;

import com.webproject.ourpoint.model.marker.Marker;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.beans.BeanUtils.copyProperties;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class MarkerDto {

  private Long markerId;

  private Long fisherId;

  private String name;

  private String latitude;

  private String longitude;

  private Boolean isPrivate;

  private String category;

  private String tags;

  private String description;

  private LocalDateTime createdAt;

  public MarkerDto(Marker source) {
    copyProperties(source, this);
  }
}

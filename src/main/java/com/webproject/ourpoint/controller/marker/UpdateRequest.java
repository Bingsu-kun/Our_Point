package com.webproject.ourpoint.controller.marker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class UpdateRequest {

  private Long markerId;

  private Long mfId;

  private String name;

  private String latitude;

  private String longitude;

  private String place_addr;

  private Boolean isPrivate;

  private String tagString;

  private String description;

}

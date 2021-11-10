package com.webproject.ourpoint.controller.marker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@NoArgsConstructor
public class AllLikedRequest {

  private List<Long> markerIds;

}

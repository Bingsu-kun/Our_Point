package com.webproject.flarepoint.controller.marker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class DeleteRequest {

  private Long markerId;

  private Long mfId;

}

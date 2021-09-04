package com.webproject.ourpoint.controller.marker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class DeleteRequestBody {

  private Long markerId;

  private Long mfId;

}

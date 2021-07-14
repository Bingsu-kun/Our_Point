package com.webproject.ourpoint.model.marker;


import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;

@Builder
@EqualsAndHashCode
@Getter
@ToString
//@Entity(name = "marker")
public class Marker {

    /*TO-DO 마커에 들어갈 내용
      마커 이름 (내가 찍은 마커 리스트에 표시될 별칭)
      x축, y축 (위도, 경도)
      작성자 id
      낚시 종류
      채비
      낚은 어종
      날짜와 시간 (시간은 아침, 점심, 저녁, 밤, 새벽으로 구분)
      날씨
    */

}

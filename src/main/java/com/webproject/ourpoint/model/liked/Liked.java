package com.webproject.ourpoint.model.liked;

import lombok.*;

import javax.persistence.*;

@Builder
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@Entity(name = "liked")
public class Liked {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "liked_id_seq")
  @SequenceGenerator(name = "liked_id_seq", sequenceName = "liked_id_seq", allocationSize = 1)
  private Long seq;

  @Column(nullable = false)
  private Long fisherId;

  @Column(nullable = false)
  private Long markerId;

  public Liked(Long fisherId, Long markerId) {
    this(null, fisherId, markerId);
  }

  public Liked(Long seq, Long fisherId, Long markerId) {
    this.seq = seq;
    this.fisherId = fisherId;
    this.markerId = markerId;
  }

}

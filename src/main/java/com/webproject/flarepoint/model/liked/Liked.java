package com.webproject.flarepoint.model.liked;

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
  private Long userId;

  @Column(nullable = false)
  private Long markerId;

  @Column(nullable = false)
  private Long mfId;

  public Liked(Long userId, Long markerId, Long mfId) {
    this(null, userId, markerId, mfId);
  }

  public Liked(Long seq, Long userId, Long markerId, Long mfId) {
    this.seq = seq;
    this.userId = userId;
    this.markerId = markerId;
    this.mfId = mfId;
  }

}

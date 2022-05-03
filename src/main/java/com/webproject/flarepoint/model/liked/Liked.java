package com.webproject.flarepoint.model.liked;

import com.webproject.flarepoint.model.marker.Marker;
import com.webproject.flarepoint.model.user.User;
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

  @ManyToOne
  @JoinColumn(name = "User_Id")
  private User user;

  @ManyToOne
  @JoinColumn(name = "Marker_MarkerId")
  private Marker marker;

  @Column(nullable = false)
  private Long mfId;

  public Liked(User user, Marker marker, Long mfId) {
    this(null, user, marker, mfId);
  }

  public Liked(Long seq, User user, Marker marker, Long mfId) {
    this.seq = seq;
    this.user = user;
    this.marker = marker;
    this.mfId = mfId;
  }

}

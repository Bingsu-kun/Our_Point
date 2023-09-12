package com.webproject.flarepoint.model.common;

import lombok.*;

import javax.persistence.*;
import javax.persistence.Id;

import static com.google.common.base.Preconditions.checkArgument;

@Getter
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@Entity(name = "tag")
@Table(uniqueConstraints = { @UniqueConstraint(name = "unq_tag", columnNames = "tag")})
public class Tag {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_id_seq")
  @SequenceGenerator(name = "tag_id_seq", sequenceName = "tag_id_seq", allocationSize = 1)
  private Long id;

  @Column(name = "tag", nullable = false)
  private String tag;

  private int used;

  public Tag(String tag) {
    this(null, tag, 0);
  }

  public Tag(Long id, String tag, int used) {
    checkArgument(tag != null, "tag must be provided.");

    this.id = id;
    this.tag = tag;
    this.used = used;
  }

  //used의 횟수를 1 올리거나 내리는 것만 허용.
  public void addUsed() {
    this.used++;
  }

  public void decreaseUsed() {
    this.used--;
  }

}

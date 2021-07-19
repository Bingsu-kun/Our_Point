package com.webproject.ourpoint.model.user;

import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkArgument;

@Getter
@ToString
@Entity
public class ConnectedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cuser_id_seq")
    @SequenceGenerator(name = "cuser_id_seq", sequenceName = "cuser_id_seq", allocationSize = 1)
    private final Long seq;

    private final String email;

    private final String name;

    private final LocalDateTime grantedAt;

    public ConnectedUser(Long seq, String email, String name, LocalDateTime grantedAt) {
        checkArgument(seq != null, "seq must be provided.");
        checkArgument(name != null, "name must be provided.");
        checkArgument(email != null, "email must be provided.");
        checkArgument(grantedAt != null, "grantedAt must be provided.");

        this.seq = seq;
        this.name = name;
        this.email = email;
        this.grantedAt = grantedAt;
    }

}

package com.webproject.flarepoint.security;

import com.webproject.flarepoint.model.common.Id;
import com.webproject.flarepoint.model.user.User;
import lombok.ToString;

import static com.google.common.base.Preconditions.checkArgument;

@ToString
public class JwtAuthentication {

  public final Id<User, Long> id;

  public final String email;

  public final String name;

  JwtAuthentication(Long id, String email, String name) {
    checkArgument(id != null, "id must be provided.");
    checkArgument(name != null, "name must be provided.");
    checkArgument(email != null, "email must be provided.");

    this.id = Id.of(User.class, id);
    this.name = name;
    this.email = email;
  }

}
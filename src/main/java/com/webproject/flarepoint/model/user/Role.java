package com.webproject.flarepoint.model.user;

public enum Role {

    ADMIN("ROLE_ADMIN"),FISHER("ROLE_FISHER"),GOODFISHER("ROLE_GOODFISHER"),GREATFISHER("ROLE_GREATFISHER");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Role of(String name) {
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(name)) {
                return role;
            }
        }
        return null;
    }

}

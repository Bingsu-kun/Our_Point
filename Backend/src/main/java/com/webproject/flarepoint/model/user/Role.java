package com.webproject.flarepoint.model.user;

public enum Role {

    ADMIN("ROLE_ADMIN"),USER("ROLE_USER"),GOODUSER("ROLE_GOODUSER"),GREATUSER("ROLE_GREATUSER");

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

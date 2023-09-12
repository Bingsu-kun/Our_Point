package com.webproject.flarepoint.model.common;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.google.common.base.Preconditions.checkArgument;

@EqualsAndHashCode
@ToString
public class Id<R, V> {

    private final Class<R> reference;

    private final V value;

    private Id(Class<R> reference, V value) {
        this.reference = reference;
        this.value = value;
    }

    public static <R, V> Id<R, V> of(Class<R> reference, V value) {
        checkArgument(reference != null, "reference must be provided.");
        checkArgument(value != null, "value must be provided.");

        return new Id<>(reference, value);
    }

    public V value() {
        return value;
    }

}

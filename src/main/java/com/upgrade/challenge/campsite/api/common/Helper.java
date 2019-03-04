package com.upgrade.challenge.campsite.api.common;

import lombok.experimental.UtilityClass;

import java.util.function.Consumer;

@UtilityClass
public class Helper {

    public <T> void setIfNotNull(final Consumer<T> consumer, final T value) {
        if (value != null) {
            consumer.accept(value);
        }
    }
}

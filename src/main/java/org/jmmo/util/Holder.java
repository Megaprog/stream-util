package org.jmmo.util;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Mutable holder to make possible have a side effect inside lambdas
 * @param <T> type of a value that the holder holds
 */
public class Holder<T> implements Serializable {
    private static final long serialVersionUID = 7632558169827026481L;

    private T value;

    public Holder() {
    }

    public Holder(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Holder<?> holder = (Holder<?>) o;
        return Objects.equals(value, holder.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public Optional<T> toOptional() {
        return Optional.ofNullable(value);
    }

    @Override
    public String toString() {
        return "Holder{" +
                "value=" + value +
                '}';
    }
}

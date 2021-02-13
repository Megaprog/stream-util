package org.jmmo.util;

import java.util.function.Supplier;

public class LazyToString {
    private final Supplier<?> supplier;

    public LazyToString(Supplier<?> supplier) {
        this.supplier = supplier;
    }

    @Override
    public String toString() {
        return String.valueOf(supplier.get());
    }

    public static LazyToString of(Supplier<?> supplier) {
        return new LazyToString(supplier);
    }
}

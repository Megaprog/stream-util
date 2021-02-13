package org.jmmo.util;

/**
 * Supplier that throw InterruptedException
 */
@FunctionalInterface
public interface InterruptableSupplier<T> {
    T get() throws InterruptedException;
}

package org.jmmo.util;

/**
 * Runnable that throw some exception
 */
@FunctionalInterface
public interface ThrowableRunnable {
    void run() throws Exception;
}

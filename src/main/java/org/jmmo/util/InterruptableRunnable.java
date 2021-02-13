package org.jmmo.util;

/**
 * Runnable that throw InterruptedException
 */
@FunctionalInterface
public interface InterruptableRunnable {
    void run() throws InterruptedException;
}

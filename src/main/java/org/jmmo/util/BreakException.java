package org.jmmo.util;

/**
 * The exception that do not collect a stack trace.
 * Useful to exit out from forEach or other stream iteration methods
 */
public class BreakException extends RuntimeException {
    private static final long serialVersionUID = -2240548921330074632L;

    public BreakException() {
        super("Break", null, false, false);
    }
}

package org.jmmo.util;

/**
 * The exception that do not collect a stack trace.
 * Useful to exit out from forEach or other stream iteration methods
 */
public class BreakException extends RuntimeException {
    private static final long serialVersionUID = -2240548921330074632L;

    private final Object value;

    public BreakException() {
        this(null);
    }

    public BreakException(Object value) {
        this("Break", value);
    }

    public BreakException(String message, Object value) {
        super(message, null, false, false);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}

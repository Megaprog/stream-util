package org.jmmo.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtil {
    private StreamUtil() {}

    public static <T> Stream<T> optional(Optional<T> optional) {
        return optional.isPresent() ? Stream.of(optional.get()) : Stream.<T>empty();
    }

    public static <T> Stream<T> supply(Supplier<T> supplier) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<T>() {
            boolean prepared;
            T current;

            @Override
            public boolean hasNext() {
                if (!prepared) {
                    prepared = true;
                    current = supplier.get();
                }
                return current != null;
            }

            @Override
            public T next() {
                if (hasNext()) {
                    prepared = false;
                    return current;
                } else {
                    throw new NoSuchElementException();
                }
            }
        }, Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL), false);
    }

    public static Stream<MatchResult> matchResults(Matcher matcher) {
        return supply(() -> matcher.find() ? matcher.toMatchResult() : null);
    }

    public static Stream<String> matchGroups(Matcher matcher) {
        return matchGroups(matcher, 1);
    }

    public static Stream<String> matchGroups(Matcher matcher, int group) {
        return supply(() -> matcher.find() ? matcher.group(group) : null);
    }

    public static Stream<Throwable> causes(Throwable throwable) {
        return supply(new Supplier<Throwable>() {
            Throwable next = throwable;

            @Override
            public Throwable get() {
                final Throwable current = next;
                if (current != null) {
                    next = current.getCause();
                }
                return current;
            }
        });
    }
}

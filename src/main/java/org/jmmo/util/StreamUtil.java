package org.jmmo.util;

import org.jmmo.util.impl.FilesIterator;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.Callable;
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

    /**
     * Finds files within a given directory and its subdirectories.
     */
    public static Stream<Path> files(Path directory) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new FilesIterator(directory), Spliterator.NONNULL), false);
    }

    /**
     * Finds files within a given directory and its subdirectories.
     * The files are filtered by matching the String representation of their file names against the given globbing pattern.
     */
    public static Stream<Path> files(Path directory, String glob) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new FilesIterator(directory, glob), Spliterator.NONNULL), false);
    }

    /**
     * Finds files within a given directory and its subdirectories.
     * The files are filtered by the given filter
     */
    public static Stream<Path> files(Path directory, DirectoryStream.Filter<Path> filter) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new FilesIterator(filter, directory), Spliterator.NONNULL), false);
    }

    /**
     * Throws checked exceptions like unchecked ones
     * @param ex any exception
     * @param <R> fake result type
     * @param <T> exception type
     * @return never returns
     * @throws T
     */
    @SuppressWarnings("unchecked")
    public static <R, T extends Throwable> R sneakyThrow(Throwable ex) throws T {
        throw (T) ex;
    }

    /**
     * Prevent necessity to check exceptions from lambdas that return some result
     * @param callable some lambda throwing checked exception
     * @param <R> result type
     * @return lambda result
     */
    public static <R> R unchecked(Callable<R> callable) {
        try {
            return callable.call();
        } catch (Exception ex) {
            return sneakyThrow(ex);
        }
    }

    /**
     * Runnable that throw some exception
     */
    @FunctionalInterface
    public interface ThrowableRunnable {
        void run() throws Exception;
    }

    /**
     * Prevent necessity to check exceptions from lambdas that don't return any result
     * @param runnable some lambda throwing checked exception
     */
    public static void unchecked(ThrowableRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            sneakyThrow(ex);
        }
    }
}

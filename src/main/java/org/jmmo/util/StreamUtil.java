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

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Stream<T> optional(Optional<T> optional) {
        return optional.map(Stream::of).orElseGet(Stream::empty);
    }

    public static <T> Stream<T> nullable(T value) {
        return value == null ? Stream.empty() : Stream.of(value);
    }

    public static <T> Stream<T> fromIterator(Iterator<T> iterator) {
        return fromIterator(iterator, 0);
    }

    public static <T> Stream<T> fromIterator(Iterator<T> iterator, int characteristics) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, characteristics), false);
    }

    public static <T> Stream<T> supply(Supplier<T> supplier) {
        return fromIterator(new Iterator<T>() {
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
        }, Spliterator.NONNULL);
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
        return fromIterator(new FilesIterator(directory), Spliterator.NONNULL);
    }

    /**
     * Finds files within a given directory and its subdirectories.
     * The files are filtered by matching the String representation of their file names against the given globbing pattern.
     */
    public static Stream<Path> files(Path directory, String glob) {
        return fromIterator(new FilesIterator(directory, glob), Spliterator.NONNULL);
    }

    /**
     * Finds files within a given directory and its subdirectories.
     * The files are filtered by the given filter
     */
    public static Stream<Path> files(Path directory, DirectoryStream.Filter<Path> filter) {
        return fromIterator(new FilesIterator(filter, directory), Spliterator.NONNULL);
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

    /**
     * Prevent necessity to check exceptions from lambdas that don't return any result,
     * then return null of required type
     * @param runnable some lambda throwing checked exception
     * @param <T> return value type
     * @return null value of T type
     */
    public static <T> T uncheckedResult(ThrowableRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            sneakyThrow(ex);
        }

        return null;
    }

    /**
     * Prevent necessity to check exceptions from lambdas that return some result and ignore result
     * @param callable some lambda throwing checked exception
     * @param <R> ignored result type
     */
    public static <R> void uncheckedIgnoreResult(Callable<R> callable) {
        try {
            callable.call();
        } catch (Exception ex) {
            sneakyThrow(ex);
        }
    }

    /**
     * Executes runnable then return null of required type
     * @param runnable some code
     * @param <T> return value type
     * @return null value of T type
     */
    public static <T> T result(Runnable runnable) {
        runnable.run();
        return null;
    }

    /**
     * Executes something that returns result and ignore result
     * @param supplier Something that returns result
     * @param <T> ignored result type
     */
    public static <T> void ignoreResult(Supplier<T> supplier) {
        supplier.get();
    }
}

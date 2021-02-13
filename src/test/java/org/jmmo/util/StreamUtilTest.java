package org.jmmo.util;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.*;

public class StreamUtilTest {

    @Test
    public void testOptional() throws Exception {
        assertEquals(0, StreamUtil.optional(Optional.empty()).count());

        final Optional<Integer> optional = Optional.of(10);
        assertEquals(Collections.singletonList(10), StreamUtil.optional(optional).collect(Collectors.toList()));
    }

    @Test
    public void testMatchGroups() throws Exception {
        final Pattern pattern = Pattern.compile("(\\d+)");
        assertEquals(0, StreamUtil.matchGroups(pattern.matcher("ABCDEFGH")).count());
        assertEquals(Arrays.asList("25", "4"), StreamUtil.matchGroups(pattern.matcher("A B25CD E 4F")).collect(Collectors.toList()));
    }

    @Test
    public void testCauses() throws Exception {
        final Exception e = new Exception();
        assertEquals(Collections.singletonList(e), StreamUtil.causes(e).collect(Collectors.toList()));

        final RuntimeException re = new RuntimeException(e);
        assertEquals(Arrays.asList(re, e), StreamUtil.causes(re).collect(Collectors.toList()));
    }

    @Test
    public void testFiles() throws Exception {
        final Path dir = Paths.get("target", "test-classes", "files");

        final Stream<Path> files = StreamUtil.files(dir, "*.txt");
        final List<Path> filesList = files.collect(Collectors.toList());
        assertThat(filesList, containsInAnyOrder(dir.resolve("text.txt"), dir.resolve("sub").resolve("sub.txt")));
    }

    @Test
    public void testDirectoriesAndFiles() throws Exception {
        final Path dir = Paths.get("target", "test-classes", "files");

        final Stream<Path> files = StreamUtil.directoriesAndFiles(dir, "sub*");
        final List<Path> filesList = files.collect(Collectors.toList());
        assertThat(filesList, contains(dir.resolve("sub").resolve("sub.txt"), dir.resolve("sub")));
    }

    @Test
    public void testDirectories() throws Exception {
        final Path dir = Paths.get("target", "test-classes", "files");

        final Stream<Path> files = StreamUtil.directories(dir);
        final List<Path> filesList = files.collect(Collectors.toList());
        assertThat(filesList, containsInAnyOrder(dir.resolve("sub")));
    }

    @Test
    public void testDirectories_empty() throws Exception {
        final Path dir = Paths.get("target", "test-classes", "files");

        final Stream<Path> files = StreamUtil.directories(dir, "*.txt");
        final List<Path> filesList = files.collect(Collectors.toList());
        assertThat(filesList, empty());
    }

    void method(int a, String b) throws IOException, InterruptedException { }
    boolean isSomething(int a, String b) throws IOException, TimeoutException { return true; }

    @Test
    public void testUnchecked_Throw() throws Exception {
        Stream.of(1)
                .filter(i1 -> StreamUtil.unchecked(() -> isSomething(i1, "1")))
                .forEach(i2 -> StreamUtil.unchecked(() -> method(i2, "2")));
    }

    @Test
    public void testUnchecked_NotThrow() throws Exception {
        Stream.of(1)
                .filter(i1 -> { try { return isSomething(i1, "1"); } catch (Exception ex) { throw new RuntimeException(ex); } })
                .forEach(i2 -> { try { method(i2, "2"); } catch (Exception ex) { throw new RuntimeException(ex); } });
    }

    @Test
    public void testLazyToString() throws Exception {
        assertEquals("-ok-", MessageFormat.format("-{0}-", new LazyToString(() -> "ok")));
        assertEquals("-5-", MessageFormat.format("-{0}-", new LazyToString(() -> 5)));
        assertEquals("-true-", MessageFormat.format("-{0}-", LazyToString.of(() -> true)));
        assertEquals("-null-", MessageFormat.format("-{0}-", LazyToString.of(() -> null)));
    }

    String wannaFunc(Function<Integer, String> function) {
        return function.apply(1);
    }

    @Test
    public void testResult() throws Exception {
        assertNull(wannaFunc((a) -> StreamUtil.resultNull(() -> {})));
    }

    @Test
    public void testUncheckedResult() throws Exception {
        assertNull(wannaFunc((a) -> StreamUtil.uncheckedNull(() -> method(1, "1"))));
    }

    @Test
    public void uninterrupted_run() {
        StreamUtil.uninterrupted(() -> Thread.sleep(1));
    }

    @Test
    public void uninterrupted_get() {
        int v =  StreamUtil.uninterrupted(() -> {Thread.sleep(1); return 1;});
        assertEquals(1, v);
    }

    @Test
    public void uninterrupted() throws InterruptedException {
        int[] intHolder = new int[1];
        boolean[] boolHolder = new boolean[1];
        Throwable[] throwableHolder = new Throwable[1];

        Thread thread = new Thread(() -> {
            int v =  StreamUtil.uninterrupted(() -> {Thread.sleep(100); return 1;});
            intHolder[0] = v;
            boolHolder[0] = Thread.interrupted();
        });
        thread.setUncaughtExceptionHandler((t, e) -> throwableHolder[0] = e);

        thread.start();
        thread.interrupt();
        thread.join();

        assertEquals(1, intHolder[0]);
        assertNull(throwableHolder[0]);
        assertTrue(boolHolder[0]);
    }
}

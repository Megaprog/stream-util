package org.jmmo.util;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

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

        assertThat(files.collect(Collectors.toList()), containsInAnyOrder(dir.resolve("text.txt"), dir.resolve("sub").resolve("sub.txt")));
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
        assertEquals("-true-", MessageFormat.format("-{0}-", new LazyToString(() -> true)));
        assertEquals("-null-", MessageFormat.format("-{0}-", new LazyToString(() -> null)));
    }

    String wannaFunc(Function<Integer, String> function) {
        return function.apply(1);
    }

    @Test
    public void testResutl() throws Exception {
        assertNull(wannaFunc((a) -> StreamUtil.resultNull(() -> {})));
    }

    @Test
    public void testUncheckedResutl() throws Exception {
        assertNull(wannaFunc((a) -> StreamUtil.uncheckedNull(() -> method(1, "1"))));
    }
}

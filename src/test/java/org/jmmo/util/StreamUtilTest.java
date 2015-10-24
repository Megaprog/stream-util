package org.jmmo.util;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
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

        final List<Path> files = StreamUtil.files(dir, "*.txt").collect(Collectors.toList());

        assertThat(files, hasSize(2));
        assertThat(files, containsInAnyOrder(dir.resolve("text.txt"), dir.resolve("sub").resolve("sub.txt")));
    }
}
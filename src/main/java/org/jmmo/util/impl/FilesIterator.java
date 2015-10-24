package org.jmmo.util.impl;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class FilesIterator implements Iterator<Path> {
    private final Function<Path, DirectoryStream<Path>> directoryStreamFunction;
    private final LinkedList<Path> directories = new LinkedList<>();
    private DirectoryStream<Path> currentStream;
    private Iterator<Path> currentIterator;
    boolean prepared;
    Path current;

    public FilesIterator(Path directory, Function<Path, DirectoryStream<Path>> directoryStreamFunction) {
        this.directoryStreamFunction = directoryStreamFunction;
        initStream(directory);
    }

    public FilesIterator(Path directory) {
        this(directory, (path) -> {
            try {
                return Files.newDirectoryStream(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public FilesIterator(Path directory, String glob) {
        this(new DirectoryStream.Filter<Path>() {
            final PathMatcher matcher = directory.getFileSystem().getPathMatcher("glob:" + glob);
            @Override
            public boolean accept(Path entry) {
                return matcher.matches(entry.getFileName());
            }
        }, directory);
    }

    public FilesIterator(DirectoryStream.Filter<Path> filter, Path directory) {
        this(directory, (path) -> {
            try {
                return Files.newDirectoryStream(path, entry -> Files.isDirectory(entry) || filter.accept(entry));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected void initStream(Path directory) {
        currentStream = directoryStreamFunction.apply(directory);
        currentIterator = currentStream.iterator();
    }

    @Override
    public boolean hasNext() {
        while (!prepared) {
            current = null;
            if (currentIterator.hasNext()) {
                current = currentIterator.next();
                if (Files.isDirectory(current)) {
                    directories.add(current);
                } else {
                    prepared = true;
                }
            } else {
                try {
                    currentStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (directories.isEmpty()) {
                    prepared = true;
                } else {
                    initStream(directories.poll());
                }
            }
        }

        return current != null;
    }

    @Override
    public Path next() {
        if (hasNext()) {
            prepared = false;
            return current;
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

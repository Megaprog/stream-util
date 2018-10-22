package org.jmmo.util.impl;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class FilesIterator implements Iterator<Path> {
    private final Path root;
    private final DirectoryStream.Filter<Path> filter;
    private final LinkedList<Path> directories = new LinkedList<>();
    private DirectoryStream<Path> currentStream;
    private Iterator<Path> currentIterator;
    private boolean prepared;
    protected Path current;
    protected Path lastDirectory;

    public FilesIterator(Path directory) {
        this(directory, (path) -> true);
    }

    public FilesIterator(Path directory, DirectoryStream.Filter<Path> filter) {
        this.root = directory;
        this.filter = filter;
        initStream(directory);
    }

    protected void initStream(Path directory) {
        try {
            currentStream = Files.newDirectoryStream(directory, filter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        currentIterator = currentStream.iterator();
        lastDirectory = directory;
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
                if (lastDirectory == null) {
                    pollNextDirectory();
                } else {
                    closeStream();
                    if (!root.equals(lastDirectory)) {
                        current = lastDirectory;
                        prepared = true;
                    }
                    lastDirectory = null;
                }
            }
        }

        return current != null;
    }

    protected void pollNextDirectory() {
        if (directories.isEmpty()) {
            prepared = true;
        } else {
            initStream(directories.poll());
        }
    }

    protected void closeStream() {
        try {
            currentStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

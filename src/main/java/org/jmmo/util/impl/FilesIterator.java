package org.jmmo.util.impl;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class FilesIterator implements Iterator<Path> {
    private final DirectoryStream.Filter<Path> filter;
    private DirectoryStream<Path> currentStream;
    private Iterator<Path> currentIterator;
    private boolean prepared;
    protected Path current;
    protected DirectoryItem lastDirectory;
    protected DirectoryItem directories;

    public FilesIterator(Path directory) {
        this(directory, (path) -> true);
    }

    public FilesIterator(Path directory, DirectoryStream.Filter<Path> filter) {
        this.filter = filter;
        initStream(new DirectoryItem(directory, null));
    }

    protected void initStream(DirectoryItem directoryItem) {
        try {
            currentStream = Files.newDirectoryStream(directoryItem.directory, filter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        currentIterator = currentStream.iterator();
        lastDirectory = directoryItem;
    }

    @Override
    public boolean hasNext() {
        while (!prepared) {
            current = null;
            if (currentIterator.hasNext()) {
                current = currentIterator.next();
                if (Files.isDirectory(current)) {
                    directories = new DirectoryItem(current, directories);
                } else {
                    prepared = true;
                }
            } else {
                if (!lastDirectory.processed) {
                    lastDirectory.processed = true;
                    closeStream();
                }
                pollNextDirectory();
            }
        }

        return current != null;
    }

    protected void pollNextDirectory() {
        if (directories == null) {
            prepared = true;
        } else if (directories.processed) {
            current = directories.directory;
            prepared = true;
            directories = directories.next;
        } else {
            initStream(directories);
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

    static class DirectoryItem {
        final Path directory;
        final DirectoryItem next;
        boolean processed;

        DirectoryItem(Path directory, DirectoryItem next) {
            this.directory = directory;
            this.next = next;
        }
    }
}

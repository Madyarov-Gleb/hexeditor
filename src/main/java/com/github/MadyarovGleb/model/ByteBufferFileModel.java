package com.github.MadyarovGleb.model;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class ByteBufferFileModel implements FileModel {
    private RandomAccessFile file;
    private FileChannel channel;
    private Path filePath;
    private boolean modified;

    public ByteBufferFileModel(Path filePath) throws IOException {
        this.filePath = filePath;
        this.file = new RandomAccessFile(filePath.toFile(), "rw");
        this.channel = file.getChannel();
        this.modified = false;
    }

    @Override
    public long getFileSize() throws IOException {
        return channel.size();
    }

    @Override
    public ByteBuffer getBytes(long offset, int length) throws IOException {
        length = (int) Math.min(length, getFileSize() - offset);
        ByteBuffer buffer = ByteBuffer.allocate(length);
        channel.read(buffer, offset);
        buffer.rewind();
        return buffer;
    }

    @Override
    public void setBytes(long offset, byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        channel.write(buffer, offset);
        modified = true;
    }

    @Override
    public String getFilePath() {
        return filePath.toString();
    }

    @Override
    public void insertBytes(long offset, byte[] data) throws IOException {
        if (offset < 0 || offset > getFileSize()) {
            throw new IllegalArgumentException("Invalid offset");
        }

        long fileSize = getFileSize();
        long newSize = fileSize + data.length;

        ByteBuffer tail = getBytes(offset, (int)(fileSize - offset));

        channel.truncate(offset);

        setBytes(offset, data);

        if (tail.hasRemaining()) {
            channel.write(tail, offset + data.length);
        }

        modified = true;
    }

    @Override
    public void deleteBytes(long offset, long length, boolean fillWithZeros) throws IOException {
        if (offset < 0 || offset + length > getFileSize()) {
            throw new IllegalArgumentException("Invalid offset or length");
        }

        if (fillWithZeros) {
            byte[] zeros = new byte[(int)length];
            setBytes(offset, zeros);
        } else {
            long fileSize = getFileSize();
            ByteBuffer tail = getBytes(offset + length, (int)(fileSize - offset - length));

            channel.truncate(offset);

            if (tail.hasRemaining()) {
                channel.write(tail, offset);
            }
        }

        modified = true;
    }

    @Override
    public void save() throws IOException {
        if (modified) {
            channel.force(true);
            modified = false;
        }
    }

    @Override
    public void saveAs(String path) throws IOException {
        throw new UnsupportedOperationException("saveAs not implemented");
    }

    @Override
    public void close() throws IOException {
        channel.close();
        file.close();
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void setByte(long position, byte value) throws IOException {
        if (position < 0 || position >= getFileSize()) {
            throw new IOException("Position out of bounds: " + position);
        }

        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put(value);
        buffer.flip();
        channel.write(buffer, position);
        modified = true;
    }
}

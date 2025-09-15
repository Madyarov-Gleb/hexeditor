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

    private static final int MOVE_BUFFER = 1 * 1024 * 1024;

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
        long size = getFileSize();
        if (offset < 0 || offset >= size) {
            return ByteBuffer.allocate(0);
        }
        if (length < 0) length = 0;
        length = (int) Math.min(length, size - offset);
        ByteBuffer buffer = ByteBuffer.allocate(length);
        channel.read(buffer, offset);
        buffer.rewind();
        return buffer;
    }

    @Override
    public void setBytes(long offset, byte[] data) throws IOException {
        if (offset < 0) throw new IOException("Negative offset");
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
        long size = getFileSize();
        long tail = size - offset;
        long newSize = size + data.length;

        file.setLength(newSize);

        long readPos = size - MOVE_BUFFER;
        long writePos = newSize - MOVE_BUFFER;
        long remaining = tail;

        ByteBuffer buf = ByteBuffer.allocate(MOVE_BUFFER);

        while (remaining > 0) {
            int chunk = (int) Math.min(MOVE_BUFFER, remaining);
            long srcPos = offset + remaining - chunk;
            long dstPos = srcPos + data.length;

            buf.clear();
            buf.limit(chunk);
            channel.read(buf, srcPos);
            buf.flip();
            channel.write(buf, dstPos);

            remaining -= chunk;
        }

        setBytes(offset, data);

        modified = true;
    }

    @Override
    public void deleteBytes(long offset, long length, boolean fillWithZeros) throws IOException {
        if (offset < 0 || length < 0 || offset + length > getFileSize()) {
            throw new IllegalArgumentException("Invalid offset or length");
        }

        if (fillWithZeros) {
            int chunkSize = MOVE_BUFFER;
            long remaining = length;
            long pos = offset;
            byte[] zeros = new byte[chunkSize];

            while (remaining > 0) {
                int chunk = (int) Math.min(chunkSize, remaining);
                ByteBuffer z = ByteBuffer.wrap(zeros, 0, chunk);
                channel.write(z, pos);
                remaining -= chunk;
                pos += chunk;
            }
        } else {
            long size = getFileSize();
            long tailOffset = offset + length;
            long tailLen = size - tailOffset;

            long remaining = tailLen;
            ByteBuffer buf = ByteBuffer.allocate(MOVE_BUFFER);

            long src = tailOffset;
            long dst = offset;

            while (remaining > 0) {
                int chunk = (int) Math.min(MOVE_BUFFER, remaining);
                buf.clear();
                buf.limit(chunk);
                channel.read(buf, src);
                buf.flip();
                channel.write(buf, dst);

                src += chunk;
                dst += chunk;
                remaining -= chunk;
            }

            file.setLength(size - length);
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
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (file != null) {
            file.close();
        }
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

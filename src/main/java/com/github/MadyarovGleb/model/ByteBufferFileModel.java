package com.github.MadyarovGleb.model;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Arrays;

public class ByteBufferFileModel implements FileModel {
    private final Path filePath;
    private byte[] data;
    private boolean modified;

    public ByteBufferFileModel(Path filePath) throws IOException {
        this.filePath = filePath;
        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            FileChannel ch = raf.getChannel();
            long size = ch.size();
            if (size > Integer.MAX_VALUE) {
                throw new IOException("Файл слишком большой");
            }
            data = new byte[(int) size];
            ByteBuffer buf = ByteBuffer.wrap(data);
            ch.read(buf);
        }
        modified = false;
    }

    @Override
    public long getFileSize() {
        return data.length;
    }

    @Override
    public ByteBuffer getBytes(long offset, int length) {
        if (offset < 0 || offset >= data.length) {
            return ByteBuffer.allocate(0);
        }
        int actualLen = (int) Math.min(length, data.length - offset);
        return ByteBuffer.wrap(Arrays.copyOfRange(data, (int) offset, (int) offset + actualLen));
    }

    @Override
    public void setBytes(long offset, byte[] bytes) throws IOException {
        if (offset < 0 || offset + bytes.length > data.length) {
            throw new IOException("Неверный диапазон записи");
        }
        System.arraycopy(bytes, 0, data, (int) offset, bytes.length);
        modified = true;
    }

    @Override
    public void setByte(long pos, byte value) throws IOException {
        if (pos < 0 || pos >= data.length) {
            throw new IOException("Выход за пределы файла");
        }
        data[(int) pos] = value;
        modified = true;
    }

    @Override
    public void insertBytes(long offset, byte[] bytes) throws IOException {
        if (offset < 0 || offset > data.length) {
            throw new IOException("Неверный offset");
        }
        byte[] newData = new byte[data.length + bytes.length];
        System.arraycopy(data, 0, newData, 0, (int) offset);
        System.arraycopy(bytes, 0, newData, (int) offset, bytes.length);
        System.arraycopy(data, (int) offset, newData, (int) offset + bytes.length, data.length - (int) offset);
        data = newData;
        modified = true;
    }

    @Override
    public void deleteBytes(long offset, long length, boolean fillWithZeros) throws IOException {
        if (offset < 0 || length < 0 || offset + length > data.length) {
            throw new IOException("Неверный offset/length");
        }
        if (fillWithZeros) {
            Arrays.fill(data, (int) offset, (int) (offset + length), (byte) 0);
        } else {
            byte[] newData = new byte[data.length - (int) length];
            System.arraycopy(data, 0, newData, 0, (int) offset);
            System.arraycopy(data, (int) (offset + length), newData, (int) offset, data.length - (int) (offset + length));
            data = newData;
        }
        modified = true;
    }

    @Override
    public void save() throws IOException {
        if (!modified) return;
        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "rw")) {
            raf.setLength(data.length);
            raf.write(data);
        }
        modified = false;
    }

    @Override
    public void saveAs(String path) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
            raf.setLength(data.length);
            raf.write(data);
        }
        modified = false;
    }

    @Override
    public void close() {
        data = null;
        modified = false;
    }

    @Override
    public String getFilePath() {
        return filePath.toString();
    }

    @Override
    public boolean isModified() {
        return modified;
    }
}

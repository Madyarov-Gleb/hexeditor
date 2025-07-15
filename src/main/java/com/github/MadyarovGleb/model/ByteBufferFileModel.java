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

        // Создаем временный файл для вставки
        long fileSize = getFileSize();
        long newSize = fileSize + data.length;

        // Копируем данные после точки вставки
        ByteBuffer tail = getBytes(offset, (int)(fileSize - offset));

        // Устанавливаем размер файла
        channel.truncate(offset);

        // Записываем новые данные
        setBytes(offset, data);

        // Записываем хвост
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
        // Реализация сохранения как нового файла
        // (для простоты опущена, можно использовать Files.copy)
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
}

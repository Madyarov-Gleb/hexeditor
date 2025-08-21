package com.github.MadyarovGleb.model;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface FileModel {
    long getFileSize() throws IOException;
    ByteBuffer getBytes(long offset, int length) throws IOException;
    void setBytes(long offset, byte[] data) throws IOException;
    void insertBytes(long offset, byte[] data) throws IOException;
    void deleteBytes(long offset, long length, boolean fillWithZeros) throws IOException;
    void save() throws IOException;
    void saveAs(String path) throws IOException;
    void close() throws IOException;
    String getFilePath();
    boolean isModified();
    void setByte(long position, byte value) throws IOException;
}

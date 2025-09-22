package com.github.MadyarovGleb;

import com.github.MadyarovGleb.controller.HexEditorController;
import com.github.MadyarovGleb.model.SelectionModel;
import com.github.MadyarovGleb.model.FileModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class HexEditorTests {

    private HexEditorController controller;
    private MockFileModel fileModel;

    static class MockFileModel implements FileModel {
        private byte[] data;
        private boolean modified;

        MockFileModel(byte[] initialData) {
            this.data = initialData;
            this.modified = false;
        }

        @Override
        public long getFileSize() { return data.length; }

        @Override
        public ByteBuffer getBytes(long offset, int length) {
            if (offset < 0 || offset >= data.length) return ByteBuffer.allocate(0);
            int actualLen = (int) Math.min(length, data.length - offset);
            return ByteBuffer.wrap(java.util.Arrays.copyOfRange(data, (int) offset, (int) offset + actualLen));
        }

        @Override
        public void setBytes(long offset, byte[] bytes) throws IOException {
            System.arraycopy(bytes, 0, data, (int) offset, bytes.length);
            modified = true;
        }

        @Override
        public void setByte(long position, byte value) throws IOException {
            data[(int) position] = value;
            modified = true;
        }

        @Override
        public void insertBytes(long offset, byte[] bytes) { modified = true; }
        @Override
        public void deleteBytes(long offset, long length, boolean fillWithZeros) { modified = true; }
        @Override
        public void save() { modified = false; }
        @Override
        public void saveAs(String path) { modified = false; }
        @Override
        public void close() { }
        @Override
        public String getFilePath() { return "mock"; }
        @Override
        public boolean isModified() { return modified; }

        public byte[] getData() { return data; }
    }

    @BeforeEach
    void setUp() {
        byte[] initialData = new byte[]{0x10, 0x20, 0x30, 0x40, 0x50};
        fileModel = new MockFileModel(initialData);
        controller = new HexEditorController(fileModel);
    }

    @Test
    void testHexEditorController_getByteAt() throws IOException {
        assertEquals(0x10, controller.getByteAt(0));
        assertEquals(0x30, controller.getByteAt(2));
        assertThrows(IOException.class, () -> controller.getByteAt(10)); // выход за границы
    }

    @Test
    void testHexEditorController_setByte() throws IOException {
        controller.getFileModel().setByte(1, (byte) 0x7F);
        assertEquals(0x7F, controller.getByteAt(1));
    }

    @Test
    void testSelectionModel_basic() {
        SelectionModel sel = controller.getSelectionModel();
        assertFalse(sel.hasSelection());

        sel.setSelection(1, 3);
        assertTrue(sel.hasSelection());
        assertEquals(1, sel.getSelectionStart());
        assertEquals(3, sel.getSelectionEnd());
        assertEquals(3, sel.getSelectionLength());

        sel.clearSelection();
        assertFalse(sel.hasSelection());
    }

    @Test
    void testController_hasUnsavedChanges() throws IOException {
        assertFalse(controller.hasUnsavedChanges());
        controller.getFileModel().setByte(0, (byte) 0xFF);
        assertTrue(controller.hasUnsavedChanges());
        controller.getFileModel().save();
        assertFalse(controller.hasUnsavedChanges());
    }

    @Test
    void testController_close() {
        assertDoesNotThrow(() -> controller.close());
    }
}

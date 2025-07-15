package com.github.MadyarovGleb.controller;

import com.github.MadyarovGleb.model.FileModel;
import com.github.MadyarovGleb.model.SelectionModel;
import javax.swing.*;
import java.nio.ByteBuffer;
import java.io.IOException;

public class HexEditorController {
    private final FileModel fileModel;
    private final SelectionModel selectionModel;

    public HexEditorController(FileModel fileModel) {
        this.fileModel = fileModel;
        this.selectionModel = new SelectionModel();
    }

    public void handleSave() throws Exception {
        fileModel.save();
    }

    public FileModel getFileModel() {
        return fileModel;
    }

    public SelectionModel getSelectionModel() {
        return selectionModel;
    }

    public boolean hasUnsavedChanges() {
        return fileModel != null && fileModel.isModified();
    }

    public void close() throws Exception {
        if (fileModel != null) {
            fileModel.close();
        }
    }

    public byte getByteAt(long position) throws IOException {
        ByteBuffer buffer = fileModel.getBytes(position, 1);
        return buffer.get();
    }
}
package com.github.MadyarovGleb.view;

import com.github.MadyarovGleb.model.FileModel;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.nio.ByteBuffer;

public class HexTableModel extends AbstractTableModel {
    private final FileModel fileModel;
    private int bytesPerRow = 16;
    private long fileOffset = 0;

    public HexTableModel(FileModel fileModel) {
        this.fileModel = fileModel;
    }

    @Override
    public int getRowCount() {
        try {
            return (int) ((fileModel.getFileSize() + bytesPerRow - 1) / bytesPerRow);
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public int getColumnCount() {
        return bytesPerRow + 1;
    }

    @Override
    public Object getValueAt(int row, int col) {
        try {
            if (col == 0) {
                return String.format("%08X", row * bytesPerRow);
            }
            long pos = row * bytesPerRow + (col - 1);
            if (pos >= fileModel.getFileSize()) return "";
            ByteBuffer buf = fileModel.getBytes(pos, 1);
            return String.format("%02X", buf.get());
        } catch (IOException e) {
            return "??";
        }
    }

    @Override
    public String getColumnName(int col) {
        return col == 0 ? "Offset" : String.format("%02X", col - 1);
    }

    public long positionForCell(int row, int column) {
        if (column == 0) return -1;
        return row * bytesPerRow + (column - 1);
    }
}
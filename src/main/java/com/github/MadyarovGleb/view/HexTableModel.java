package com.github.MadyarovGleb.view;

import com.github.MadyarovGleb.model.FileModel;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.nio.ByteBuffer;

public class HexTableModel extends AbstractTableModel {
    private final FileModel fileModel;

    private int bytesPerRow = 16;
    private int rowsPerPage = 64;
    private long fileOffset = 0;

    public HexTableModel(FileModel fileModel) {
        this.fileModel = fileModel;
    }

    @Override
    public int getRowCount() {
        return rowsPerPage;
    }

    @Override
    public int getColumnCount() {
        return bytesPerRow + 1;
    }

    @Override
    public Object getValueAt(int row, int col) {
        try {
            if (col == 0) {
                long absoluteRowOffset = fileOffset + (long) row * bytesPerRow;
                return String.format("%08X", absoluteRowOffset);
            }
            long pos = fileOffset + (long) row * bytesPerRow + (col - 1);
            if (pos < 0 || pos >= fileModel.getFileSize()) return "";
            ByteBuffer buf = fileModel.getBytes(pos, 1);
            if (buf.remaining() == 0) return "";
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
        return fileOffset + (long) row * bytesPerRow + (column - 1);
    }

    public void setBytesPerRow(int bpr) {
        if (bpr < 1) bpr = 1;
        this.bytesPerRow = bpr;
        fireTableStructureChanged();
    }

    public void setRowsPerPage(int rpp) {
        if (rpp < 1) rpp = 1;
        this.rowsPerPage = rpp;
        fireTableStructureChanged();
    }

    public void setFileOffset(long offset) {
        if (offset < 0) offset = 0;
        this.fileOffset = offset;
        fireTableDataChanged();
    }

    public long getFileOffset() {
        return fileOffset;
    }

    public int getBytesPerRow() {
        return bytesPerRow;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public long getMaxOffset() throws IOException {
        long size = fileModel.getFileSize();
        long pageBytes = (long) bytesPerRow * rowsPerPage;
        if (size <= pageBytes) return 0;
        return size - pageBytes;
    }
}

package com.github.MadyarovGleb.view;

import com.github.MadyarovGleb.model.FileModel;
import com.github.MadyarovGleb.model.SelectionModel;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;

public class HexEditorPanel extends JPanel {
    private JTable hexTable;
    private HexTableModel tableModel;
    private JLabel statusBar;
    private FileModel fileModel; // Добавляем прямое хранение fileModel

    public HexEditorPanel() {
        initEmptyUI();
    }

    public void setModel(FileModel fileModel, SelectionModel selectionModel) {
        this.fileModel = fileModel; // Сохраняем fileModel
        removeAll();
        try {
            initHexView(fileModel, selectionModel);
        } catch (IOException e) {
            showError("Error loading file: " + e.getMessage());
            initEmptyUI();
        }
        revalidate();
        repaint();
    }

    private void initEmptyUI() {
        setLayout(new BorderLayout());
        add(new JLabel("Open a file to begin editing", SwingConstants.CENTER), BorderLayout.CENTER);
        statusBar = new JLabel(" No file loaded ");
        add(statusBar, BorderLayout.SOUTH);
    }

    private void initHexView(FileModel fileModel, SelectionModel selectionModel) throws IOException {
        setLayout(new BorderLayout());

        tableModel = new HexTableModel(fileModel);
        hexTable = new JTable(tableModel);
        configureTable();

        add(new JScrollPane(hexTable), BorderLayout.CENTER);

        statusBar = new JLabel(" " + fileModel.getFilePath() + " | Size: " +
                formatSize(fileModel.getFileSize()) + " ");
        add(statusBar, BorderLayout.SOUTH);

        // Добавляем слушатели для всех изменений выделения
        hexTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateByteValue();
            }
        });
        hexTable.getColumnModel().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateByteValue();
            }
        });
    }

    private void updateByteValue() {
        int row = hexTable.getSelectedRow();
        int col = hexTable.getSelectedColumn();

        if (row >= 0 && col >= 1) { // col >= 1 чтобы игнорировать колонку адресов
            try {
                long pos = tableModel.positionForCell(row, col);
                ByteBuffer buffer = fileModel.getBytes(pos, 1);
                byte b = buffer.get();

                String status = String.format("Byte: %d (signed) | %d (unsigned) | Hex: %02X",
                        b,
                        Byte.toUnsignedInt(b),
                        b);
                statusBar.setText(status);

            } catch (IOException ex) {
                statusBar.setText("Error reading byte");
            }
        }
    }

    private void configureTable() {
        hexTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        hexTable.setCellSelectionEnabled(true);
        hexTable.setFont(new Font("Monospaced", Font.PLAIN, 14));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < hexTable.getColumnCount(); i++) {
            hexTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            hexTable.getColumnModel().getColumn(i).setPreferredWidth(30);
        }
        hexTable.getColumnModel().getColumn(0).setPreferredWidth(80);
    }

    private String formatSize(long size) {
        if (size < 1024) return size + " bytes";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
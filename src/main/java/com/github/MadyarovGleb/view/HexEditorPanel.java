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
    private FileModel fileModel;

    public HexEditorPanel() {
        initEmptyUI();
    }

    public void setModel(FileModel fileModel, SelectionModel selectionModel) {
        this.fileModel = fileModel;
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
        setupContextMenu();

        add(new JScrollPane(hexTable), BorderLayout.CENTER);

        statusBar = new JLabel(" " + fileModel.getFilePath() + " | Size: " +
                formatSize(fileModel.getFileSize()) + " ");
        add(statusBar, BorderLayout.SOUTH);

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

        if (row >= 0 && col >= 1) {
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

    private void setupContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem byteItem = new JMenuItem("View as byte");
        byteItem.addActionListener(e -> showSelectedValue(1));
        contextMenu.add(byteItem);

        JMenuItem shortItem = new JMenuItem("View as 2 bytes (short)");
        shortItem.addActionListener(e -> showSelectedValue(2));
        contextMenu.add(shortItem);

        JMenuItem intItem = new JMenuItem("View as 4 bytes (int/float)");
        intItem.addActionListener(e -> showSelectedValue(4));
        contextMenu.add(intItem);

        JMenuItem longItem = new JMenuItem("View as 8 bytes (long/double)");
        longItem.addActionListener(e -> showSelectedValue(8));
        contextMenu.add(longItem);

        hexTable.setComponentPopupMenu(contextMenu);
    }

    private void showValueDialog(long position, int byteCount) {
        try {
            byteCount = (int) Math.min(byteCount, fileModel.getFileSize() - position);

            ByteBuffer buffer = fileModel.getBytes(position, byteCount);
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            ValueInterpretationDialog dialog = new ValueInterpretationDialog(parentFrame, data, position);
            dialog.setVisible(true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error reading data: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showSelectedValue(int byteCount) {
        int row = hexTable.getSelectedRow();
        int col = hexTable.getSelectedColumn();

        if (row >= 0 && col >= 1) {
            long pos = tableModel.positionForCell(row, col);
            showValueDialog(pos, byteCount);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a cell first",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}